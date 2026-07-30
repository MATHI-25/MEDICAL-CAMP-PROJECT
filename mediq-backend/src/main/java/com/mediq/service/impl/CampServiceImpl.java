package com.mediq.service.impl;

import com.mediq.constants.CampStatus;
import com.mediq.constants.UserRole;
import com.mediq.dto.AssignStaffRequest;
import com.mediq.dto.CampResponse;
import com.mediq.dto.CreateCampRequest;
import com.mediq.dto.UpdateCampRequest;
import com.mediq.entity.MedicalCamp;
import com.mediq.entity.User;
import com.mediq.exception.BadRequestException;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.CampMapper;
import com.mediq.repository.CampRepository;
import com.mediq.repository.UserRepository;
import com.mediq.service.CampService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampServiceImpl implements CampService {

    private final CampRepository campRepository;
    private final UserRepository userRepository;
    private final CampMapper campMapper;

    @Override
    @Transactional
    public CampResponse createCamp(CreateCampRequest request) {
        String campCode = request.getCampCode();
        if (campCode == null || campCode.isBlank()) {
            long count = campRepository.count() + 1;
            campCode = String.format("CAMP-%d-%03d", LocalDate.now().getYear(), count);
        } else if (campRepository.existsByCampCodeAndIsDeletedFalse(campCode)) {
            throw new BadRequestException("Camp code '" + campCode + "' already exists");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Camp end date cannot be before start date");
        }

        String sTime = request.getStartTime() != null ? request.getStartTime() : "09:00 AM";
        String eTime = request.getEndTime() != null ? request.getEndTime() : "05:00 PM";
        String opHours = request.getOperatingHours() != null ? request.getOperatingHours() : (sTime + " - " + eTime);

        MedicalCamp camp = MedicalCamp.builder()
                .campCode(campCode)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .venue(request.getVenue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(sTime)
                .endTime(eTime)
                .operatingHours(opHours)
                .targetCapacity(request.getTargetCapacity() != null ? request.getTargetCapacity() : 500)
                .status(request.getStatus() != null ? request.getStatus() : CampStatus.UPCOMING)
                .assignedDoctors(fetchStaffUsers(request.getDoctorIds(), UserRole.DOCTOR))
                .assignedNurses(fetchStaffUsers(request.getNurseIds(), UserRole.NURSE))
                .assignedVolunteers(fetchStaffUsers(request.getVolunteerIds(), UserRole.REGISTRATION_VOLUNTEER))
                .build();

        MedicalCamp savedCamp = campRepository.save(camp);
        return campMapper.toResponse(savedCamp);
    }

    @Override
    @Transactional
    public CampResponse updateCamp(Long campId, UpdateCampRequest request) {
        MedicalCamp camp = findCampById(campId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Camp end date cannot be before start date");
        }

        camp.setTitle(request.getTitle());
        camp.setDescription(request.getDescription());
        camp.setLocation(request.getLocation());
        camp.setVenue(request.getVenue());
        camp.setStartDate(request.getStartDate());
        camp.setEndDate(request.getEndDate());
        camp.setTargetCapacity(request.getTargetCapacity());
        camp.setStatus(request.getStatus());

        MedicalCamp updatedCamp = campRepository.save(camp);
        return campMapper.toResponse(updatedCamp);
    }

    @Override
    @Transactional(readOnly = true)
    public CampResponse getCampById(Long campId) {
        return campMapper.toResponse(findCampById(campId));
    }

    @Override
    @Transactional(readOnly = true)
    public CampResponse getCampByCode(String campCode) {
        MedicalCamp camp = campRepository.findByCampCodeAndIsDeletedFalse(campCode)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "campCode", campCode));
        return campMapper.toResponse(camp);
    }

    @Override
    @Transactional
    public CampResponse assignStaff(Long campId, AssignStaffRequest request) {
        MedicalCamp camp = findCampById(campId);

        if (request.getDoctorIds() != null) {
            camp.setAssignedDoctors(fetchStaffUsers(request.getDoctorIds(), UserRole.DOCTOR));
        }

        if (request.getNurseIds() != null) {
            camp.setAssignedNurses(fetchStaffUsers(request.getNurseIds(), UserRole.NURSE));
        }

        if (request.getVolunteerIds() != null) {
            camp.setAssignedVolunteers(fetchStaffUsers(request.getVolunteerIds(), UserRole.REGISTRATION_VOLUNTEER));
        }

        MedicalCamp savedCamp = campRepository.save(camp);
        return campMapper.toResponse(savedCamp);
    }

    @Override
    @Transactional
    public CampResponse updateCampStatus(Long campId, CampStatus status) {
        MedicalCamp camp = findCampById(campId);
        camp.setStatus(status);
        return campMapper.toResponse(campRepository.save(camp));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampResponse> getCampsByStatus(CampStatus status) {
        return campRepository.findByStatusAndIsDeletedFalseOrderByStartDateAsc(status)
                .stream()
                .map(campMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampResponse> searchCamps(CampStatus status, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return campRepository.searchCamps(status, keyword, pageable)
                .map(campMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteCamp(Long campId) {
        MedicalCamp camp = findCampById(campId);
        camp.setDeleted(true);
        campRepository.save(camp);
    }

    private MedicalCamp findCampById(Long campId) {
        return campRepository.findById(campId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", campId));
    }

    private Set<User> fetchStaffUsers(Set<Long> userIds, UserRole requiredRole) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashSet<>();
        }

        List<User> users = userRepository.findAllById(userIds);
        Set<User> filteredUsers = users.stream()
                .filter(u -> !u.isDeleted() && u.isActive() && u.getRole() == requiredRole)
                .collect(Collectors.toSet());

        return filteredUsers;
    }
}
