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

export const billingService = {
  getAllInvoices: () => apiClient.get('/billing/invoices'),
  getInvoiceById: (id) => apiClient.get(`/billing/invoices/${id}`),
  getInvoicesByPatient: (email) => apiClient.get(`/billing/invoices/patient/${email}`),
  getInvoicesByStatus: (status) => apiClient.get(`/billing/invoices/status/${status}`),
  createInvoice: (invoice) => apiClient.post('/billing/invoices', invoice),
  updateInvoice: (id, invoice) => apiClient.put(`/billing/invoices/${id}`, invoice),
  getPaymentsByInvoice: (invoiceId) => apiClient.get(`/billing/payments/invoice/${invoiceId}`),
  recordPayment: (payment) => apiClient.post('/billing/payments', payment),
  getOutstandingBalance: (email) => apiClient.get(`/billing/outstanding/${email}`)
};
