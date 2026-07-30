export const ROLES = {
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  ORGANIZER: 'ORGANIZER',
  REGISTRATION_VOLUNTEER: 'REGISTRATION_VOLUNTEER',
  NURSE: 'NURSE',
  DOCTOR: 'DOCTOR',
  PHARMACY: 'PHARMACY',
};

export const DEFAULT_CREDENTIALS = [
  { role: ROLES.SYSTEM_ADMIN, memberId: 'MC-ADM-001', label: 'System Admin' },
  { role: ROLES.ORGANIZER, memberId: 'MC-ORG-001', label: 'Camp Organizer' },
  { role: ROLES.DOCTOR, memberId: 'MC-DOC-001', label: 'Medical Doctor' },
  { role: ROLES.NURSE, memberId: 'MC-NUR-001', label: 'Staff Nurse' },
  { role: ROLES.PHARMACY, memberId: 'MC-PHA-001', label: 'Pharmacy Staff' },
  { role: ROLES.REGISTRATION_VOLUNTEER, memberId: 'MC-REG-001', label: 'Registration Volunteer' },
];
