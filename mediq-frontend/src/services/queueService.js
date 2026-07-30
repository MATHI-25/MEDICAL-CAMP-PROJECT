import api from './api';

export const queueService = {
  generateToken: async (data) => api.post('/queue/generate', data),
  updateTokenStatus: async (id, status, assignedDoctorId) =>
    api.patch(`/queue/${id}/status`, { status, assignedDoctorId }),
  assignDoctor: async (id, doctorId) => api.patch(`/queue/${id}/assign-doctor`, null, { params: { doctorId } }),
  getTokenById: async (id) => api.get(`/queue/${id}`),
  getNurseQueue: async (campId) => api.get(`/queue/nurse/${campId}`),
  getDoctorQueue: async (campId, doctorId) => api.get(`/queue/doctor/${campId}`, { params: { doctorId } }),
  getTokensByStatus: async (campId, status) => api.get(`/queue/status/${campId}`, { params: { status } }),
  getQueueDashboard: async (campId) => api.get(`/queue/dashboard/${campId}`),
  cancelToken: async (id) => api.delete(`/queue/${id}`),
};

export default queueService;
