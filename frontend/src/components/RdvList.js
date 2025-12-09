import React, { useState, useEffect } from 'react';
import { rdvService } from '../services/api';

function RdvList() {
  const [rdvs, setRdvs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
              <div className="rdv-info">
                <h3>{rdv.patientNom} {rdv.patientPrenom}</h3>
                <p><strong>Date:</strong> {formatDate(rdv.dateHeure)}</p>
                <p><strong>Motif:</strong> {rdv.motif}</p>
                <p><strong>Email:</strong> {rdv.patientEmail}</p>
                <p><strong>Téléphone:</strong> {rdv.patientTelephone}</p>
              </div>
              <button
                className="btn btn-danger"
                onClick={() => handleDelete(rdv.id)}
              >
                Annuler
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default RdvList;
