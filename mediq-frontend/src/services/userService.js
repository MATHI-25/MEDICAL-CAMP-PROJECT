import api from './api';

export const userService = {
  createUser: async (data) => api.post('/users', data),
  updateUser: async (id, data) => api.put(`/users/${id}`, data),
  getUserById: async (id) => api.get(`/users/${id}`),
  getUserByMemberId: async (memberId) => api.get(`/users/member/${memberId}`),
  getUsersByRole: async (role) => api.get(`/users/role/${role}`),
  searchUsers: async (role, keyword, page = 0, size = 10) =>
    api.get('/users', { params: { role, keyword, page, size } }),
  activateUser: async (id) => api.patch(`/users/${id}/activate`),
  deactivateUser: async (id) => api.patch(`/users/${id}/deactivate`),
  deleteUser: async (id) => api.delete(`/users/${id}`),
  resetPassword: async (id, newPassword) => api.patch(`/users/${id}/reset-password`, { newPassword }),
};

export default userService;
