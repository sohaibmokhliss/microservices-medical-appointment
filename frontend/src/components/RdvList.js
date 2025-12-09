import React, { useState, useEffect } from 'react';
import { rdvService } from '../services/api';

function RdvList() {
  const [rdvs, setRdvs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editData, setEditData] = useState({});

  useEffect(() => {
    loadRdvs();
  }, []);

  const loadRdvs = async () => {
    try {
      setLoading(true);
      const response = await rdvService.getAllRdv();
      setRdvs(response.data);
      setError(null);
    } catch (err) {
      setError('Erreur lors du chargement des rendez-vous');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (rdv) => {
    setEditingId(rdv.id);
    setEditData({
      patientNom: rdv.patientNom,
      patientPrenom: rdv.patientPrenom,
      patientEmail: rdv.patientEmail,
      patientTelephone: rdv.patientTelephone,
      dateHeure: rdv.dateHeure,
      motif: rdv.motif,
      statut: rdv.statut,
      docteurId: rdv.docteurId
    });
  };

  const handleUpdate = async (id) => {
    try {
      await rdvService.updateRdv(id, editData);
      setEditingId(null);
      loadRdvs();
    } catch (err) {
      alert('Erreur lors de la modification du rendez-vous');
      console.error(err);
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setEditData({});
  };

  const handleInputChange = (e) => {
    setEditData({
      ...editData,
      [e.target.name]: e.target.value
    });
  };

  const handleDelete = async (id) => {
    if (window.confirm('Voulez-vous vraiment annuler ce rendez-vous?')) {
      try {
        await rdvService.deleteRdv(id);
        loadRdvs();
      } catch (err) {
        alert('Erreur lors de l\'annulation du rendez-vous');
        console.error(err);
      }
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return <div className="loading">Chargement des rendez-vous...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div>
      <h2>Mes Rendez-vous</h2>

      {rdvs.length === 0 ? (
        <p>Aucun rendez-vous trouvé</p>
      ) : (
        <div className="rdv-list">
          {rdvs.map((rdv) => (
            <div key={rdv.id} className="rdv-card">
              {editingId === rdv.id ? (
                <div className="rdv-edit-form">
                  <h3>Modifier le rendez-vous</h3>
                  <div className="form-group">
                    <label>Nom:</label>
                    <input
                      type="text"
                      name="patientNom"
                      value={editData.patientNom}
                      onChange={handleInputChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>Prénom:</label>
                    <input
                      type="text"
                      name="patientPrenom"
                      value={editData.patientPrenom}
                      onChange={handleInputChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>Email:</label>
                    <input
                      type="email"
                      name="patientEmail"
                      value={editData.patientEmail}
                      onChange={handleInputChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>Téléphone:</label>
                    <input
                      type="tel"
                      name="patientTelephone"
                      value={editData.patientTelephone}
                      onChange={handleInputChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>Date et heure:</label>
                    <input
                      type="datetime-local"
                      name="dateHeure"
                      value={editData.dateHeure}
                      onChange={handleInputChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>Motif:</label>
                    <textarea
                      name="motif"
                      value={editData.motif}
                      onChange={handleInputChange}
                    />
                  </div>
                  <div className="form-group">
                    <label>Statut:</label>
                    <select name="statut" value={editData.statut} onChange={handleInputChange}>
                      <option value="CONFIRMÉ">CONFIRMÉ</option>
                      <option value="EN_ATTENTE">EN_ATTENTE</option>
                      <option value="ANNULÉ">ANNULÉ</option>
                    </select>
                  </div>
                  <div className="form-actions">
                    <button className="btn btn-success" onClick={() => handleUpdate(rdv.id)}>
                      Enregistrer
                    </button>
                    <button className="btn btn-secondary" onClick={handleCancel}>
                      Annuler
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  <div className="rdv-info">
                    <h3>{rdv.patientNom} {rdv.patientPrenom}</h3>
                    <p><strong>Date:</strong> {formatDate(rdv.dateHeure)}</p>
                    <p><strong>Motif:</strong> {rdv.motif}</p>
                    <p><strong>Email:</strong> {rdv.patientEmail}</p>
                    <p><strong>Téléphone:</strong> {rdv.patientTelephone}</p>
                    <p><strong>Statut:</strong> {rdv.statut}</p>
                  </div>
                  <div className="rdv-actions">
                    <button
                      className="btn btn-warning"
                      onClick={() => handleEdit(rdv)}
                    >
                      Modifier
                    </button>
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(rdv.id)}
                    >
                      Annuler
                    </button>
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default RdvList;
