import apiClient from './apiClient';

export const authService = {
  login: (credentials) => apiClient.post('/auth/login', credentials),
  // register: (payload) => apiClient.post('/auth/register', payload), // Disabled - users created via DataInitializer
  getCurrentUser: () => apiClient.get('/auth/me')
};
