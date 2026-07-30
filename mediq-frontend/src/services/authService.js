import api from './api';

export const authService = {
  login: async (memberId, password, role) => {
    return await api.post('/auth/login', { memberId, password, role });
  },
  getCurrentUser: async () => {
    return await api.get('/auth/me');
  },
  logout: async () => {
    return await api.post('/auth/logout');
  },
};

export default authService;
