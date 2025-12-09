import React, { useState, useEffect } from 'react';
import { docteurService, rdvService, notificationService } from '../services/api';

function RdvForm() {
  const [docteurs, setDocteurs] = useState([]);
  const [formData, setFormData] = useState({
    docteurId: '',
    patientNom: '',
    patientPrenom: '',
    patientEmail: '',
    patientTelephone: '',
    dateHeure: '',
    motif: ''
  });
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadDocteurs();
  }, []);

  const loadDocteurs = async () => {
    try {
      const response = await docteurService.getAllDocteurs();
      const docteursData = response.data._embedded?.docteurs || [];
      // Extract ID from self link if not present
      const docteursWithIds = docteursData.map(doc => {
        if (!doc.id && doc._links?.self?.href) {
          const id = doc._links.self.href.split('/').pop();
          return { ...doc, id: parseInt(id) };
        }
        return doc;
      });
      setDocteurs(docteursWithIds);
    } catch (err) {
      console.error('Erreur lors du chargement des docteurs', err);
    }
  };

  const handleChange = (e) => {
    const value = e.target.name === 'docteurId' ? Number(e.target.value) : e.target.value;
    setFormData({
      ...formData,
      [e.target.name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      const rdvData = {
        ...formData,
        docteurId: Number(formData.docteurId)
      };
      const rdv = await rdvService.createRdv(rdvData);

      // Try to send notification, but don't fail if it doesn't work
      try {
        await notificationService.sendNotification({
          type: 'EMAIL',
          destination: formData.patientEmail,
          subject: 'Confirmation de rendez-vous',
          message: `Votre rendez-vous a été confirmé pour le ${formData.dateHeure}`
        });
        setMessage({ type: 'success', text: 'Rendez-vous créé avec succès! Email de confirmation envoyé.' });
      } catch (notifErr) {
        console.error('Erreur lors de l\'envoi de la notification', notifErr);
        setMessage({ type: 'success', text: 'Rendez-vous créé avec succès! (Email non envoyé)' });
      }
      setFormData({
        docteurId: '',
        patientNom: '',
        patientPrenom: '',
        patientEmail: '',
        patientTelephone: '',
        dateHeure: '',
        motif: ''
      });
    } catch (err) {
      setMessage({ type: 'error', text: 'Erreur lors de la création du rendez-vous' });
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Prendre un Rendez-vous</h2>

      {message && (
        <div className={message.type}>{message.text}</div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="docteurId">Choisir un docteur *</label>
          <select
            id="docteurId"
            name="docteurId"
            value={formData.docteurId}
            onChange={handleChange}
            required
          >
            <option value="">Sélectionnez un docteur</option>
            {docteurs.map((docteur) => (
              <option key={docteur.id} value={docteur.id}>
                Dr. {docteur.nom} {docteur.prenom} - {docteur.specialite}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="patientNom">Nom *</label>
          <input
            type="text"
            id="patientNom"
            name="patientNom"
            value={formData.patientNom}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="patientPrenom">Prénom *</label>
          <input
            type="text"
            id="patientPrenom"
            name="patientPrenom"
            value={formData.patientPrenom}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="patientEmail">Email *</label>
          <input
            type="email"
            id="patientEmail"
            name="patientEmail"
            value={formData.patientEmail}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="patientTelephone">Téléphone *</label>
          <input
            type="tel"
            id="patientTelephone"
            name="patientTelephone"
            value={formData.patientTelephone}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="dateHeure">Date et Heure *</label>
          <input
            type="datetime-local"
            id="dateHeure"
            name="dateHeure"
            value={formData.dateHeure}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="motif">Motif de consultation *</label>
          <textarea
            id="motif"
            name="motif"
            value={formData.motif}
            onChange={handleChange}
            required
          />
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Création en cours...' : 'Créer le rendez-vous'}
        </button>
      </form>
    </div>
  );
}

export default RdvForm;
