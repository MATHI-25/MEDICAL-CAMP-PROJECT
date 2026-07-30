import api from './api';

export const doctorService = {
  saveConsultation: async (data) => api.post('/doctor/consultations', data),
  getConsultationById: async (id) => api.get(`/doctor/consultations/${id}`),
  getConsultationByCode: async (code) => api.get(`/doctor/consultations/code/${code}`),
  getPatientConsultationHistory: async (patientId) => api.get(`/doctor/consultations/patient/${patientId}`),
  getDoctorConsultations: async (campId, doctorId) =>
    api.get(`/doctor/consultations/camp/${campId}`, { params: { doctorId } }),
};

export default doctorService;
