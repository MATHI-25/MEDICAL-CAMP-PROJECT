import api from './api';

export const nurseService = {
  recordVitals: async (data) => api.post('/nurse/vitals', data),
  getVitalsById: async (id) => api.get(`/nurse/vitals/${id}`),
  getVitalsByTokenId: async (tokenId) => api.get(`/nurse/vitals/token/${tokenId}`),
  getPatientVitalsHistory: async (patientId) => api.get(`/nurse/vitals/patient/${patientId}`),
};

export default nurseService;
