import apiClient from './apiClient';

export const docteurService = {
  getAllDocteurs: () => apiClient.get('/docteurs'),
  getDocteurById: (id) => apiClient.get(`/docteurs/${id}`),
  createDocteur: (docteur) => apiClient.post('/docteurs', docteur),
  updateDocteur: (id, docteur) => apiClient.put(`/docteurs/${id}`, docteur),
  deleteDocteur: (id) => apiClient.delete(`/docteurs/${id}`)
};

export const rdvService = {
  getAllRdv: () => apiClient.get('/rdv'),
  getRdvById: (id) => apiClient.get(`/rdv/${id}`),
  getRdvByDocteur: (docteurId) => apiClient.get(`/rdv/docteur/${docteurId}`),
  createRdv: (rdv) => apiClient.post('/rdv', rdv),
  updateRdv: (id, rdv) => apiClient.put(`/rdv/${id}`, rdv),
  deleteRdv: (id) => apiClient.delete(`/rdv/${id}`)
};

export const notificationService = {
  sendNotification: (notification) => apiClient.post('/notifications/send', notification)
};

export const userService = {
  getAllUsers: () => apiClient.get('/users'),
  getUserById: (id) => apiClient.get(`/users/${id}`),
  createUser: (user) => apiClient.post('/users', user),
  updateUser: (id, updates) => apiClient.put(`/users/${id}`, updates),
  deleteUser: (id) => apiClient.delete(`/users/${id}`),
  toggleUserStatus: (id) => apiClient.patch(`/users/${id}/toggle`)
};
