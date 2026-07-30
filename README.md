# MediQ – Digital Prescription & Medical Camp Management System

MediQ is an enterprise-grade digital prescription, queue routing, clinical vitals tracking, pharmacy inventory, and hospital referral management system designed for organized medical camps and outreach health operations.

---

## 🚀 Key Features

- **Multi-Role Security & RBAC**: Dedicated dashboards for 6 distinct system roles (`SYSTEM_ADMIN`, `ORGANIZER`, `DOCTOR`, `NURSE`, `PHARMACY`, `REGISTRATION_VOLUNTEER`).
- **Patient Intake & ID Generation**: Automated formatting for unique Patient IDs (`PAT-YYYY-XXXX`) and printable intake passes.
- **Queue Management**: Real-time token sequencing (`TKN-001`), estimated wait time calculation, nurse routing, and doctor queue dispatch.
- **Nurse Vitals Station**: Clinical recording for BP, Pulse, SpO2, Temp, Blood Sugar, Pain Scale (0–10), automated BMI computation, and doctor forwarding.
- **Doctor Consultation Workspace**: Diagnostic recording, disease identification, lab recommendations, and clinical decision branching.
- **Digital Prescription Engine**: Auto-generated Prescription Codes (`RX-YYYY-XXXX`), medicine dosage schedules, and printable PDF documents rendered via OpenPDF.
- **Hospital Referral Engine (CORE FEATURE)**: Referral ID auto-formatting (`REF-YYYY-XXXX`), destination hospital/specialist tracking, urgency levels (`NORMAL`, `URGENT`, `CRITICAL`), and printable official Referral Letter PDFs.
- **Pharmacy & Inventory Module**: Stock deduction, low stock alerts, batch expiry warnings, and full/partial dispensing tracking.
- **Executive Analytics & CSV Exporters**: Real-time camp analytics, doctor workload reports, referral analysis, and downloadable CSV data exports.

---

## 🔑 Default System User Credentials

| Role | Member ID | Default Password | Access Scope |
|---|---|---|---|
| **System Administrator** | `MC-ADM-001` | `Camp@2026` | Full User & System Administration |
| **Camp Organizer** | `MC-ORG-001` | `Camp@2026` | Camp Scheduling & Staff Assignment |
| **Doctor** | `MC-DOC-001` | `Camp@2026` | Consultation, Prescriptions & Referrals |
| **Nurse** | `MC-NUR-001` | `Camp@2026` | Vitals Recording & BMI Calculation |
| **Pharmacist** | `MC-PHA-001` | `Camp@2026` | Medicine Inventory & Dispensing |
| **Registration Volunteer** | `MC-REG-001` | `Camp@2026` | Patient Registration & Queue Tokens |

---

## 🛠️ Technology Stack

### Backend (`mediq-backend`)
- **Language & Runtime**: Java 21, Spring Boot 3.3.0
- **Security**: Spring Security, JWT (JJW 0.12.5), HMAC-SHA256
- **Database & ORM**: MySQL 8.0, Spring Data JPA, Hibernate, Liquibase/schema DDL
- **Document Rendering**: OpenPDF 1.3.30
- **Documentation**: Swagger / OpenAPI 3.0 (`/swagger-ui.html`)
- **Build Tool**: Apache Maven 3.9+

### Frontend (`mediq-frontend`)
- **Framework**: React 18, Vite 5
- **Styling**: Tailwind CSS, Lucide React icons
- **HTTP Client**: Axios with JWT Interceptors
- **Routing**: React Router v6 with Role Guards

---

## 📖 Master REST API Reference Table

| Module | Method | Endpoint | Description |
|---|---|---|---|
| **Auth** | POST | `/api/v1/auth/login` | User login & JWT token generation |
| **Auth** | GET | `/api/v1/auth/me` | Fetch authenticated user profile |
| **Users** | POST | `/api/v1/users` | Create staff user (Admin only) |
| **Users** | GET | `/api/v1/users` | List / search system users |
| **Camps** | POST | `/api/v1/camps` | Create new medical camp |
| **Camps** | POST | `/api/v1/camps/{id}/staff` | Assign doctors, nurses, volunteers |
| **Patients**| POST | `/api/v1/patients` | Register patient & issue Patient ID |
| **Queue** | POST | `/api/v1/queue/generate` | Issue queue token (`TKN-001`) |
| **Nurse** | POST | `/api/v1/nurse/vitals` | Record vitals & forward to doctor |
| **Doctor** | POST | `/api/v1/doctor/consultations` | Save consultation & diagnosis |
| **Prescriptions** | POST | `/api/v1/prescriptions` | Issue digital prescription (`RX-YYYY-XXXX`) |
| **Prescriptions** | GET | `/api/v1/prescriptions/{id}/pdf` | Stream Digital Prescription PDF |
| **Referrals** | POST | `/api/v1/referrals` | Issue hospital referral (`REF-YYYY-XXXX`) |
| **Referrals** | GET | `/api/v1/referrals/{id}/pdf` | Stream Hospital Referral Letter PDF |
| **Pharmacy** | POST | `/api/v1/pharmacy/inventory` | Add medicine stock item |
| **Pharmacy** | POST | `/api/v1/pharmacy/dispense` | Dispense medicines & complete queue |
| **Reports** | GET | `/api/v1/reports/camp/{id}` | Get camp analytics summary |
| **Reports** | GET | `/api/v1/reports/export/patients/csv` | Export patients CSV report |
| **Search** | GET | `/api/v1/search?query=...` | Unified multi-entity global search |

---

## 🏃 Running the Application

### 1. Start Backend Server
```bash
cd mediq-backend
mvn spring-boot:run
```
- Backend runs on `http://localhost:8080`
- Swagger UI available at `http://localhost:8080/swagger-ui.html`

### 2. Start Frontend Application
```bash
cd mediq-frontend
npm install
npm run dev
```
- Frontend runs on `http://localhost:5173`

---

## 📜 License & Compliance
Built for Medical Outreach Operations. All clinical data ledgers are immutable and soft-deleted for audit compliance.
