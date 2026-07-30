package com.mediq.service.impl;

import com.mediq.constants.PrescriptionStatus;
import com.mediq.constants.QueueStatus;
import com.mediq.dto.*;
import com.mediq.entity.*;
import com.mediq.exception.BadRequestException;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.DispenseMapper;
import com.mediq.mapper.MedicineMapper;
import com.mediq.repository.*;
import com.mediq.service.MedicalHistoryService;
import com.mediq.service.PharmacyService;
import com.mediq.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl implements PharmacyService {

    private final MedicineInventoryRepository medicineRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository itemRepository;
    private final DispenseRecordRepository dispenseRepository;
    private final UserRepository userRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final MedicineMapper medicineMapper;
    private final DispenseMapper dispenseMapper;
    private final MedicalHistoryService medicalHistoryService;

    @Override
    @Transactional
    public MedicineResponse addMedicine(AddMedicineRequest request) {
        if (medicineRepository.existsByMedicineCodeAndIsDeletedFalse(request.getMedicineCode())) {
            throw new BadRequestException("Medicine code '" + request.getMedicineCode() + "' already exists");
        }

        MedicineInventory medicine = MedicineInventory.builder()
                .medicineCode(request.getMedicineCode())
                .name(request.getName())
                .category(request.getCategory())
                .batchNumber(request.getBatchNumber())
                .manufacturer(request.getManufacturer())
                .expiryDate(request.getExpiryDate())
                .stockQuantity(request.getStockQuantity())
                .minAlertQuantity(request.getMinAlertQuantity() != null ? request.getMinAlertQuantity() : 20)
                .unitPrice(request.getUnitPrice() != null ? request.getUnitPrice() : 0.0)
                .build();

        MedicineInventory saved = medicineRepository.save(medicine);
        return medicineMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MedicineResponse updateStock(Long medicineId, UpdateStockRequest request) {
        MedicineInventory medicine = findMedicineById(medicineId);
        medicine.setStockQuantity(medicine.getStockQuantity() + request.getAddedQuantity());
        return medicineMapper.toResponse(medicineRepository.save(medicine));
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(Long medicineId) {
        return medicineMapper.toResponse(findMedicineById(medicineId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .filter(m -> !m.isDeleted())
                .map(medicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> searchMedicines(String keyword) {
        return medicineRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword)
                .stream()
                .map(medicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getLowStockAlerts() {
        return medicineRepository.findLowStockMedicines()
                .stream()
                .map(medicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<DispenseRecordResponse> dispenseMedicines(DispenseMedicineRequest request) {
        Prescription prescription = prescriptionRepository.findById(request.getPrescriptionId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", request.getPrescriptionId()));

        String currentMemberId = SecurityUtils.getCurrentUserMemberId()
                .orElseThrow(() -> new ResourceNotFoundException("Logged in pharmacist session missing"));

        User pharmacist = userRepository.findByMemberIdAndIsDeletedFalse(currentMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", currentMemberId));

        List<DispenseRecord> createdRecords = new ArrayList<>();

        for (DispenseItemRequest itemReq : request.getItems()) {
            PrescriptionItem item = itemRepository.findById(itemReq.getPrescriptionItemId())
                    .filter(i -> !i.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("PrescriptionItem", "id", itemReq.getPrescriptionItemId()));

            MedicineInventory medicine = null;
            if (itemReq.getMedicineId() != null) {
                medicine = findMedicineById(itemReq.getMedicineId());
            } else {
                List<MedicineInventory> matches = medicineRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(item.getMedicineName());
                if (!matches.isEmpty()) {
                    medicine = matches.get(0);
                }
            }

            if (medicine != null) {
                if (medicine.getStockQuantity() < itemReq.getQuantityToDispense()) {
                    throw new BadRequestException(String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                            medicine.getName(), medicine.getStockQuantity(), itemReq.getQuantityToDispense()));
                }
                medicine.setStockQuantity(medicine.getStockQuantity() - itemReq.getQuantityToDispense());
                medicineRepository.save(medicine);
            }

            item.setQuantityDispensed(item.getQuantityDispensed() + itemReq.getQuantityToDispense());
            itemRepository.save(item);

            DispenseRecord record = DispenseRecord.builder()
                    .prescription(prescription)
                    .prescriptionItem(item)
                    .medicine(medicine)
                    .pharmacist(pharmacist)
                    .quantityDispensed(itemReq.getQuantityToDispense())
                    .remarks(itemReq.getRemarks())
                    .build();

            createdRecords.add(dispenseRepository.save(record));
        }

        // Evaluate Prescription Dispensing Status
        boolean allFullyDispensed = prescription.getItems().stream()
                .allMatch(i -> i.getQuantityDispensed() >= i.getQuantityPrescribed());

        if (allFullyDispensed) {
            prescription.setStatus(PrescriptionStatus.DISPENSED);
            // Complete Patient Queue Token
            QueueToken token = prescription.getConsultation().getQueueToken();
            if (token != null) {
                token.setStatus(QueueStatus.COMPLETED);
                queueTokenRepository.save(token);
            }
        } else {
            prescription.setStatus(PrescriptionStatus.PARTIALLY_DISPENSED);
        }
        prescriptionRepository.save(prescription);

        // Record dispense event in medical history timeline
        medicalHistoryService.recordEvent(
                prescription.getPatient(),
                "DISPENSE",
                String.format("Medicines Dispensed for Prescription: %s", prescription.getPrescriptionCode()),
                String.format("Dispensed by Pharmacist %s. Status: %s.", pharmacist.getFullName(), prescription.getStatus().name()),
                prescription.getPrescriptionCode(),
                pharmacist.getFullName()
        );

        return createdRecords.stream()
                .map(dispenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispenseRecordResponse> getDispenseRecordsForPrescription(Long prescriptionId) {
        return dispenseRepository.findByPrescriptionIdAndIsDeletedFalse(prescriptionId)
                .stream()
                .map(dispenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispenseRecordResponse> getPharmacistDispenseHistory() {
        String currentMemberId = SecurityUtils.getCurrentUserMemberId()
                .orElseThrow(() -> new ResourceNotFoundException("Logged in pharmacist session missing"));

        User pharmacist = userRepository.findByMemberIdAndIsDeletedFalse(currentMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", currentMemberId));

        return dispenseRepository.findByPharmacistIdAndIsDeletedFalseOrderByDispenseDateDesc(pharmacist.getId())
                .stream()
                .map(dispenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    private MedicineInventory findMedicineById(Long medicineId) {
        return medicineRepository.findById(medicineId)
                .filter(m -> !m.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicineInventory", "id", medicineId));
    }
}
