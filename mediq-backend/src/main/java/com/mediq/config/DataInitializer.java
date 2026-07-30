package com.mediq.config;

import com.mediq.constants.*;
import com.mediq.entity.*;
import com.mediq.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CampRepository campRepository;
    private final PatientRepository patientRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final PatientVitalsRepository vitalsRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineInventoryRepository medicineRepository;
    private final HospitalReferralRepository referralRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedDefaultUsers();
        seedDefaultMedicines();
        seedSampleCampWorkflow();
    }

    private void seedDefaultUsers() {
        seedUserIfNotExists("MC-ADM-001", "Camp@2026", "System Administrator", "admin@mediq.health", "+1-800-555-0100", UserRole.SYSTEM_ADMIN, "System Administration", "IT Operations");
        seedUserIfNotExists("MC-ORG-001", "Camp@2026", "Chief Organizer", "organizer@mediq.health", "+1-800-555-0101", UserRole.ORGANIZER, "Medical Operations", "Camp Logistics");
        seedUserIfNotExists("MC-DOC-001", "Camp@2026", "Dr. Sarah Jenkins", "doctor@mediq.health", "+1-800-555-0102", UserRole.DOCTOR, "General Medicine", "Outpatient Department");
        seedUserIfNotExists("MC-NUR-001", "Camp@2026", "Nurse Clara Barton", "nurse@mediq.health", "+1-800-555-0103", UserRole.NURSE, "Clinical Vitals", "Nursing Services");
        seedUserIfNotExists("MC-PHA-001", "Camp@2026", "Pharmacist Alexander Fleming", "pharmacy@mediq.health", "+1-800-555-0104", UserRole.PHARMACY, "Clinical Pharmacology", "Camp Pharmacy");
        seedUserIfNotExists("MC-REG-001", "Camp@2026", "Volunteer Registration Staff", "volunteer@mediq.health", "+1-800-555-0105", UserRole.REGISTRATION_VOLUNTEER, "Patient Intake", "Front Desk");
    }

    private void seedUserIfNotExists(String memberId, String rawPassword, String fullName, String email, String phone, UserRole role, String specialization, String department) {
        if (!userRepository.existsByMemberIdAndIsDeletedFalse(memberId)) {
            User user = User.builder()
                    .memberId(memberId)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .email(email)
                    .phone(phone)
                    .role(role)
                    .specialization(specialization)
                    .department(department)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info("Seeded default system user: {} with role: {}", memberId, role);
        }
    }

    private void seedDefaultMedicines() {
        seedMedicineIfNotExists("MED-PCM-500", "Paracetamol 500mg", "Analgesic & Antipyretic", "BCH-2026-01", "GSK Pharma", 500, 50, 0.50);
        seedMedicineIfNotExists("MED-AMX-500", "Amoxicillin 500mg", "Antibiotic", "BCH-2026-02", "Pfizer Labs", 300, 30, 2.00);
        seedMedicineIfNotExists("MED-CTZ-10", "Cetirizine 10mg", "Antihistamine", "BCH-2026-03", "Cipla Health", 400, 40, 0.75);
        seedMedicineIfNotExists("MED-MET-500", "Metformin 500mg", "Anti-Diabetic", "BCH-2026-04", "Sun Pharma", 250, 25, 1.25);
        seedMedicineIfNotExists("MED-OMP-20", "Omeprazole 20mg", "Antacid / PPI", "BCH-2026-05", "Dr. Reddy's", 350, 30, 1.50);
        seedMedicineIfNotExists("MED-AZM-500", "Azithromycin 500mg", "Macrolide Antibiotic", "BCH-2026-06", "Lupin Pharma", 200, 20, 3.50);
        seedMedicineIfNotExists("MED-IBU-400", "Ibuprofen 400mg", "NSAID / Anti-inflammatory", "BCH-2026-07", "Abbott Labs", 450, 40, 0.80);
    }

    private void seedMedicineIfNotExists(String code, String name, String category, String batch, String manufacturer, int stock, int minAlert, double price) {
        if (!medicineRepository.existsByMedicineCodeAndIsDeletedFalse(code)) {
            MedicineInventory medicine = MedicineInventory.builder()
                    .medicineCode(code)
                    .name(name)
                    .category(category)
                    .batchNumber(batch)
                    .manufacturer(manufacturer)
                    .expiryDate(LocalDate.now().plusYears(2))
                    .stockQuantity(stock)
                    .minAlertQuantity(minAlert)
                    .unitPrice(price)
                    .build();
            medicineRepository.save(medicine);
            log.info("Seeded default medicine inventory item: {} ({})", name, code);
        }
    }

    private void seedSampleCampWorkflow() {
        if (!campRepository.findByStatusAndIsDeletedFalseOrderByStartDateAsc(CampStatus.ONGOING).isEmpty()) {
            return;
        }

        User doctor = userRepository.findByMemberIdAndIsDeletedFalse("MC-DOC-001").orElse(null);
        User nurse = userRepository.findByMemberIdAndIsDeletedFalse("MC-NUR-001").orElse(null);
        User volunteer = userRepository.findByMemberIdAndIsDeletedFalse("MC-REG-001").orElse(null);

        if (doctor == null || nurse == null || volunteer == null) return;

        // 1. Create Active Medical Camp
        MedicalCamp camp = MedicalCamp.builder()
                .campCode("CAMP-2026-001")
                .title("Sector 12 Free Community Health Camp")
                .description("Comprehensive medical outreach providing free vitals screening, doctor consultation, digital prescriptions, and hospital referrals.")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(2))
                .location("Community Health Centre, Sector 12")
                .targetCapacity(250)
                .status(CampStatus.ONGOING)
                .build();
        camp.getAssignedDoctors().add(doctor);
        camp.getAssignedNurses().add(nurse);
        camp.getAssignedVolunteers().add(volunteer);
        camp = campRepository.save(camp);

        // 2. Register Sample Patients
        Patient p1 = patientRepository.save(Patient.builder()
                .patientId("PAT-2026-0001")
                .fullName("Robert Miller")
                .age(45)
                .gender("MALE")
                .bloodGroup("O_POSITIVE")
                .phone("+1-555-0199")
                .address("42 Maple Street, Sector 12")
                .emergencyContact("+1-555-0198")
                .allergies("Penicillin")
                .chronicDiseases("Hypertension")
                .registeredCamp(camp)
                .build());

        Patient p2 = patientRepository.save(Patient.builder()
                .patientId("PAT-2026-0002")
                .fullName("Eleanor Vance")
                .age(38)
                .gender("FEMALE")
                .bloodGroup("A_POSITIVE")
                .phone("+1-555-0211")
                .address("88 Oak Avenue, Sector 12")
                .emergencyContact("+1-555-0210")
                .allergies("None")
                .chronicDiseases("Asthma")
                .registeredCamp(camp)
                .build());

        // 3. Issue Queue Tokens
        QueueToken t1 = queueTokenRepository.save(QueueToken.builder()
                .tokenNumber("TKN-001")
                .sequenceNumber(1)
                .patient(p1)
                .camp(camp)
                .status(QueueStatus.SENT_TO_PHARMACY)
                .assignedDoctor(doctor)
                .estimatedWaitMinutes(5)
                .build());

        QueueToken t2 = queueTokenRepository.save(QueueToken.builder()
                .tokenNumber("TKN-002")
                .sequenceNumber(2)
                .patient(p2)
                .camp(camp)
                .status(QueueStatus.REFERRED_TO_HOSPITAL)
                .assignedDoctor(doctor)
                .estimatedWaitMinutes(10)
                .build());

        // 4. Record Patient Vitals
        PatientVitals v1 = vitalsRepository.save(PatientVitals.builder()
                .patient(p1)
                .camp(camp)
                .queueToken(t1)
                .recordedByNurse(nurse)
                .heightCm(175.0)
                .weightKg(72.0)
                .bmi(23.5)
                .temperatureF(98.6)
                .bloodPressure("120/80")
                .pulseRate(74)
                .spo2Percent(98)
                .bloodSugarMgDl(105.0)
                .painScale(0)
                .nurseNotes("Vitals stable. Alert and oriented.")
                .build());

        PatientVitals v2 = vitalsRepository.save(PatientVitals.builder()
                .patient(p2)
                .camp(camp)
                .queueToken(t2)
                .recordedByNurse(nurse)
                .heightCm(162.0)
                .weightKg(58.0)
                .bmi(22.1)
                .temperatureF(99.2)
                .bloodPressure("135/88")
                .pulseRate(88)
                .spo2Percent(95)
                .bloodSugarMgDl(120.0)
                .painScale(3)
                .nurseNotes("Patient complaining of shortness of breath and mild wheezing.")
                .build());

        // 5. Doctor Consultations
        Consultation c1 = consultationRepository.save(Consultation.builder()
                .consultationCode("CNS-2026-0001")
                .patient(p1)
                .doctor(doctor)
                .camp(camp)
                .queueToken(t1)
                .vitals(v1)
                .diseaseName("Upper Respiratory Tract Infection")
                .diagnosisNotes("Mild fever, cough, and throat irritation.")
                .labTestRecommendations("Complete Blood Count (CBC) if symptoms persist.")
                .doctorNotes("Advised rest, warm fluids, and 5-day antibiotic course.")
                .requiresReferral(false)
                .build());

        Consultation c2 = consultationRepository.save(Consultation.builder()
                .consultationCode("CNS-2026-0002")
                .patient(p2)
                .doctor(doctor)
                .camp(camp)
                .queueToken(t2)
                .diseaseName("Acute Bronchial Asthma Exacerbation")
                .diagnosisNotes("Severe wheezing and shortness of breath.")
                .labTestRecommendations("Chest X-Ray, Spirometry, ABG.")
                .doctorNotes("Immediate hospital referral for nebulization and pulmonary evaluation.")
                .requiresReferral(true)
                .build());

        // 6. Digital Prescription
        Prescription rx1 = Prescription.builder()
                .prescriptionCode("RX-2026-0001")
                .consultation(c1)
                .patient(p1)
                .doctor(doctor)
                .camp(camp)
                .status(PrescriptionStatus.CREATED)
                .generalInstructions("Take medicines after meals with warm water.")
                .doctorSignature("Dr. Sarah Jenkins")
                .build();

        rx1.addItem(PrescriptionItem.builder()
                .medicineName("Amoxicillin 500mg")
                .dosage("500mg")
                .frequency("1-0-1 After Meals")
                .duration("5 Days")
                .instructions("Finish complete course")
                .quantityPrescribed(10)
                .quantityDispensed(0)
                .build());

        rx1.addItem(PrescriptionItem.builder()
                .medicineName("Paracetamol 500mg")
                .dosage("500mg")
                .frequency("1-1-1 As Needed")
                .duration("3 Days")
                .instructions("For fever reduction")
                .quantityPrescribed(9)
                .quantityDispensed(0)
                .build());

        prescriptionRepository.save(rx1);

        // 7. Hospital Referral
        referralRepository.save(HospitalReferral.builder()
                .referralId("REF-2026-0001")
                .patient(p2)
                .doctor(doctor)
                .camp(camp)
                .consultation(c2)
                .reason("Advanced pulmonary evaluation & nebulization treatment.")
                .recommendedTests("Chest X-Ray, Spirometry, Pulmonary Function Test")
                .hospitalName("City General Government Hospital")
                .hospitalAddress("100 Healthcare Boulevard, Central District")
                .department("Pulmonology & Respiratory Care")
                .specialistType("Pulmonologist")
                .doctorNotes("Patient experiencing acute asthma flare-up.")
                .urgency("URGENT")
                .status(ReferralStatus.CREATED)
                .build());

        // 8. Default Medicine Inventory Seed
        if (medicineRepository.count() == 0) {
            medicineRepository.save(MedicineInventory.builder()
                    .medicineCode("MED-001")
                    .name("Amoxicillin 500mg")
                    .category("Antibiotics")
                    .batchNumber("BCH-2026-01")
                    .manufacturer("Cipla Healthcare")
                    .expiryDate(LocalDate.now().plusYears(2))
                    .stockQuantity(350)
                    .minAlertQuantity(50)
                    .unitPrice(2.50)
                    .build());

            medicineRepository.save(MedicineInventory.builder()
                    .medicineCode("MED-002")
                    .name("Paracetamol 500mg")
                    .category("Analgesic / Antipyretic")
                    .batchNumber("BCH-2026-02")
                    .manufacturer("Sun Pharma")
                    .expiryDate(LocalDate.now().plusYears(3))
                    .stockQuantity(500)
                    .minAlertQuantity(100)
                    .unitPrice(0.80)
                    .build());

            medicineRepository.save(MedicineInventory.builder()
                    .medicineCode("MED-003")
                    .name("Metformin 500mg")
                    .category("Anti-Diabetic")
                    .batchNumber("BCH-2026-03")
                    .manufacturer("Lupin Labs")
                    .expiryDate(LocalDate.now().plusYears(2))
                    .stockQuantity(250)
                    .minAlertQuantity(40)
                    .unitPrice(1.20)
                    .build());

            medicineRepository.save(MedicineInventory.builder()
                    .medicineCode("MED-004")
                    .name("Cetirizine 10mg")
                    .category("Antihistamine")
                    .batchNumber("BCH-2026-04")
                    .manufacturer("Dr. Reddy's")
                    .expiryDate(LocalDate.now().plusYears(2))
                    .stockQuantity(180)
                    .minAlertQuantity(30)
                    .unitPrice(0.50)
                    .build());

            medicineRepository.save(MedicineInventory.builder()
                    .medicineCode("MED-005")
                    .name("Amlodipine 5mg")
                    .category("Cardiovascular / Antihypertensive")
                    .batchNumber("BCH-2026-05")
                    .manufacturer("Torrent Pharma")
                    .expiryDate(LocalDate.now().plusYears(2))
                    .stockQuantity(15)
                    .minAlertQuantity(30)
                    .unitPrice(1.50)
                    .build());
        }

        log.info("Seeded complete End-to-End Medical Camp workflow scenario (Camp: CAMP-2026-001, RX: RX-2026-0001, REF: REF-2026-0001)");
    }
}
