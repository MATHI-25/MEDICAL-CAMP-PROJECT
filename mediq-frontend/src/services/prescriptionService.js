import api from './api';

export const prescriptionService = {
  createPrescription: async (data) => api.post('/prescriptions', data),
  getPrescriptionById: async (id) => api.get(`/prescriptions/${id}`),
  getPrescriptionByCode: async (code) => api.get(`/prescriptions/code/${code}`),
  getPrescriptionByConsultationId: async (consultationId) => api.get(`/prescriptions/consultation/${consultationId}`),
  getPatientPrescriptions: async (patientId) => api.get(`/prescriptions/patient/${patientId}`),
  getPrescriptionsByStatus: async (campId, status) => api.get(`/prescriptions/status/${campId}`, { params: { status } }),
  getPrescriptionPdfUrl: (id) => `/api/v1/prescriptions/${id}/pdf`,
  downloadPrescriptionPdf: async (id) => api.get(`/prescriptions/${id}/pdf`, { responseType: 'blob' }),
};

export default prescriptionService;
