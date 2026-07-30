-- MediQ Database Schema DDL
-- MySQL 8.0 Compatible Schema

CREATE DATABASE IF NOT EXISTS mediq_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mediq_db;

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    specialization VARCHAR(100),
    department VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_user_member_id (member_id),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. MEDICAL CAMPS TABLE
CREATE TABLE IF NOT EXISTS camps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    camp_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    venue VARCHAR(150),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    target_capacity INT DEFAULT 500,
    status VARCHAR(30) NOT NULL DEFAULT 'UPCOMING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_camp_code (camp_code),
    INDEX idx_camp_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. CAMP STAFF ASSIGNMENT JOIN TABLES
CREATE TABLE IF NOT EXISTS camp_doctors (
    camp_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (camp_id, user_id),
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS camp_nurses (
    camp_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (camp_id, user_id),
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS camp_volunteers (
    camp_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (camp_id, user_id),
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. PATIENTS TABLE
CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    blood_group VARCHAR(20),
    phone VARCHAR(20),
    address TEXT,
    emergency_contact VARCHAR(100),
    allergies TEXT,
    chronic_diseases TEXT,
    registered_camp_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (registered_camp_id) REFERENCES camps(id) ON DELETE SET NULL,
    INDEX idx_patient_id (patient_id),
    INDEX idx_patient_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. QUEUE TOKENS TABLE
CREATE TABLE IF NOT EXISTS queue_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_number VARCHAR(20) NOT NULL,
    sequence_number INT NOT NULL,
    patient_id BIGINT NOT NULL,
    camp_id BIGINT NOT NULL,
    assigned_doctor_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    estimated_wait_minutes INT DEFAULT 15,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_doctor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_token_camp (camp_id),
    INDEX idx_token_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. PATIENT VITALS TABLE
CREATE TABLE IF NOT EXISTS patient_vitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    queue_token_id BIGINT NOT NULL,
    camp_id BIGINT NOT NULL,
    recorded_by_nurse_id BIGINT NOT NULL,
    height_cm DOUBLE,
    weight_kg DOUBLE,
    bmi DOUBLE,
    temperature_f DOUBLE,
    blood_pressure VARCHAR(20),
    pulse_rate INT,
    respiratory_rate INT,
    blood_sugar_mg_dl DOUBLE,
    spo2_percent INT,
    symptoms TEXT,
    pain_scale INT DEFAULT 0,
    nurse_notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (queue_token_id) REFERENCES queue_tokens(id) ON DELETE CASCADE,
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by_nurse_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. CONSULTATIONS TABLE
CREATE TABLE IF NOT EXISTS consultations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consultation_code VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    camp_id BIGINT NOT NULL,
    queue_token_id BIGINT NOT NULL,
    vitals_id BIGINT,
    disease_name VARCHAR(150),
    diagnosis_notes TEXT NOT NULL,
    lab_test_recommendations TEXT,
    doctor_notes TEXT,
    follow_up_date DATE,
    requires_referral BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    FOREIGN KEY (queue_token_id) REFERENCES queue_tokens(id) ON DELETE CASCADE,
    FOREIGN KEY (vitals_id) REFERENCES patient_vitals(id) ON DELETE SET NULL,
    INDEX idx_consultation_code (consultation_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. PRESCRIPTIONS TABLE
CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_code VARCHAR(50) NOT NULL UNIQUE,
    consultation_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    camp_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    general_instructions TEXT,
    doctor_signature VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (consultation_id) REFERENCES consultations(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    INDEX idx_prescription_code (prescription_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. PRESCRIPTION ITEMS TABLE
CREATE TABLE IF NOT EXISTS prescription_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    instructions VARCHAR(255),
    quantity_prescribed INT NOT NULL,
    quantity_dispensed INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. MEDICINE INVENTORY TABLE
CREATE TABLE IF NOT EXISTS medicine_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    batch_number VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(100),
    expiry_date DATE NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_alert_quantity INT NOT NULL DEFAULT 20,
    unit_price DOUBLE DEFAULT 0.0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_medicine_code (medicine_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. DISPENSE RECORDS TABLE
CREATE TABLE IF NOT EXISTS dispense_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    prescription_item_id BIGINT NOT NULL,
    medicine_id BIGINT,
    pharmacist_id BIGINT NOT NULL,
    quantity_dispensed INT NOT NULL,
    dispense_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (prescription_item_id) REFERENCES prescription_items(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicine_inventory(id) ON DELETE SET NULL,
    FOREIGN KEY (pharmacist_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. HOSPITAL REFERRALS TABLE
CREATE TABLE IF NOT EXISTS hospital_referrals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referral_id VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    camp_id BIGINT NOT NULL,
    consultation_id BIGINT NOT NULL,
    reason TEXT NOT NULL,
    recommended_tests TEXT,
    hospital_name VARCHAR(150) NOT NULL,
    hospital_address TEXT,
    department VARCHAR(100) NOT NULL,
    specialist_type VARCHAR(100),
    doctor_notes TEXT,
    current_medicines TEXT,
    urgency VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    follow_up_date DATE,
    remarks TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (camp_id) REFERENCES camps(id) ON DELETE CASCADE,
    FOREIGN KEY (consultation_id) REFERENCES consultations(id) ON DELETE CASCADE,
    INDEX idx_referral_id (referral_id),
    INDEX idx_referral_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. MEDICAL HISTORIES TABLE
CREATE TABLE IF NOT EXISTS medical_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_summary VARCHAR(255) NOT NULL,
    event_details TEXT,
    reference_code VARCHAR(50),
    performed_by VARCHAR(100),
    event_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    INDEX idx_history_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
