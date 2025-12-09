import React, { useState, useEffect } from 'react';
import { docteurService } from '../services/api';

function DocteurList() {
  const [docteurs, setDocteurs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDocteurs();
  }, []);

  const loadDocteurs = async () => {
    try {
      setLoading(true);
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
      setError(null);
    } catch (err) {
      setError('Erreur lors du chargement des docteurs');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Chargement des docteurs...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div>
      <h2>Liste des Docteurs</h2>
      <div className="docteur-grid">
        {docteurs.map((docteur) => (
          <div key={docteur.id} className="docteur-card">
            <h3>Dr. {docteur.nom} {docteur.prenom}</h3>
            <p><strong>Spécialité:</strong> {docteur.specialite}</p>
            <p><strong>Email:</strong> {docteur.email}</p>
            <p><strong>Téléphone:</strong> {docteur.telephone}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default DocteurList;
