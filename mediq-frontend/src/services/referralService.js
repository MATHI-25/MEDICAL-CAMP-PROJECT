import api from './api';

export const referralService = {
  createReferral: async (data) => api.post('/referrals', data),
  updateReferralStatus: async (id, status, remarks) => api.patch(`/referrals/${id}/status`, { status, remarks }),
  getReferralById: async (id) => api.get(`/referrals/${id}`),
  getReferralByCode: async (referralId) => api.get(`/referrals/code/${referralId}`),
  getReferralByConsultationId: async (consultationId) => api.get(`/referrals/consultation/${consultationId}`),
  getPatientReferrals: async (patientId) => api.get(`/referrals/patient/${patientId}`),
  getReferralsByStatus: async (campId, status) => api.get(`/referrals/status/${campId}`, { params: { status } }),
  getReferralPdfUrl: (id) => `/api/v1/referrals/${id}/pdf`,
  downloadReferralPdf: async (id) => api.get(`/referrals/${id}/pdf`, { responseType: 'blob' }),
};

export default referralService;
