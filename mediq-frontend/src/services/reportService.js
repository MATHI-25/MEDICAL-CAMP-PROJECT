import api from './api';

export const reportService = {
  getCampAnalytics: async (campId) => api.get(`/reports/camp/${campId}`),
  getDoctorWorkloadReport: async (campId) => api.get(`/reports/doctors/${campId}`),
  getMedicineConsumptionReport: async () => api.get('/reports/medicines'),
  getReferralReport: async (campId) => api.get(`/reports/referrals/${campId}`),
  globalSearch: async (query) => api.get('/search', { params: { query } }),
  getPatientsCsvUrl: (campId) => `/api/v1/reports/export/patients/csv${campId ? `?campId=${campId}` : ''}`,
  getMedicinesCsvUrl: () => '/api/v1/reports/export/medicines/csv',
  downloadPatientsCsv: async (campId) => api.get(`/reports/export/patients/csv${campId ? `?campId=${campId}` : ''}`, { responseType: 'blob' }),
  downloadMedicinesCsv: async () => api.get('/reports/export/medicines/csv', { responseType: 'blob' }),
};

export default reportService;
