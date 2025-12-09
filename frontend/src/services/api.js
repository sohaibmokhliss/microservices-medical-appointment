import axios from 'axios';

const DOCTEUR_API_URL = 'http://localhost:8081/api';
const RDV_API_URL = 'http://localhost:8082/api';
const NOTIFICATION_API_URL = 'http://localhost:8083/api';

export const docteurService = {
  getAllDocteurs: () => axios.get(`${DOCTEUR_API_URL}/docteurs`),
  getDocteurById: (id) => axios.get(`${DOCTEUR_API_URL}/docteurs/${id}`)
};

export const rdvService = {
  getAllRdv: () => axios.get(`${RDV_API_URL}/rdv`),
  getRdvById: (id) => axios.get(`${RDV_API_URL}/rdv/${id}`),
  getRdvByDocteur: (docteurId) => axios.get(`${RDV_API_URL}/rdv/docteur/${docteurId}`),
  createRdv: (rdv) => axios.post(`${RDV_API_URL}/rdv`, rdv),
  updateRdv: (id, rdv) => axios.put(`${RDV_API_URL}/rdv/${id}`, rdv),
  deleteRdv: (id) => axios.delete(`${RDV_API_URL}/rdv/${id}`)
};

export const notificationService = {
  sendNotification: (notification) => axios.post(`${NOTIFICATION_API_URL}/notifications/send`, notification)
};
