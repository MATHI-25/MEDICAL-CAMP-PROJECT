import api from './api';

export const patientService = {
  registerPatient: async (data) => api.post('/patients', data),
  updatePatient: async (id, data) => api.put(`/patients/${id}`, data),
  getPatientById: async (id) => api.get(`/patients/${id}`),
  getPatientByPatientId: async (patientId) => api.get(`/patients/patient-id/${patientId}`),
  getPatientsByPhone: async (phone) => api.get(`/patients/phone/${phone}`),
  searchPatients: async (campId, keyword, page = 0, size = 10) =>
    api.get('/patients', { params: { campId, keyword, page, size } }),
  getPatientTimeline: async (id) => api.get(`/patients/${id}/timeline`),
  deletePatient: async (id) => api.delete(`/patients/${id}`),
};

export default patientService;
