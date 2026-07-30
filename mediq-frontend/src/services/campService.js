import api from './api';

export const campService = {
  createCamp: async (data) => api.post('/camps', data),
  updateCamp: async (id, data) => api.put(`/camps/${id}`, data),
  getCampById: async (id) => api.get(`/camps/${id}`),
  getCampByCode: async (campCode) => api.get(`/camps/code/${campCode}`),
  assignStaff: async (id, staffData) => api.post(`/camps/${id}/staff`, staffData),
  updateCampStatus: async (id, status) => api.patch(`/camps/${id}/status`, null, { params: { status } }),
  getCampsByStatus: async (status) => api.get(`/camps/status/${status}`),
  searchCamps: async (status, keyword, page = 0, size = 10) =>
    api.get('/camps', { params: { status, keyword, page, size } }),
  deleteCamp: async (id) => api.delete(`/camps/${id}`),
};

export default campService;
