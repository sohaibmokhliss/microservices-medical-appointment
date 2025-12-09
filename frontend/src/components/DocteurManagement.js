import React, { useState, useEffect } from 'react';
import { docteurService } from '../services/api';

function DocteurManagement() {
  const [docteurs, setDocteurs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [formData, setFormData] = useState({
    nom: '',
    prenom: '',
    specialite: '',
    email: '',
    telephone: ''
  });

  useEffect(() => {
    loadDocteurs();
  }, []);

  const loadDocteurs = async () => {
    try {
      setLoading(true);
      const response = await docteurService.getAllDocteurs();
      setDocteurs(response.data);
      setError('');
    } catch (err) {
      setError('Erreur lors du chargement des docteurs');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      await docteurService.createDocteur(formData);
      setFormData({ nom: '', prenom: '', specialite: '', email: '', telephone: '' });
      setShowAddForm(false);
      loadDocteurs();
      setError('');
    } catch (err) {
      setError('Erreur lors de la création du docteur');
      console.error(err);
    }
  };

  const handleEdit = (docteur) => {
    setEditingId(docteur.id);
    setFormData({
      nom: docteur.nom,
      prenom: docteur.prenom,
      specialite: docteur.specialite,
      email: docteur.email,
      telephone: docteur.telephone
    });
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await docteurService.updateDocteur(editingId, formData);
      setEditingId(null);
      setFormData({ nom: '', prenom: '', specialite: '', email: '', telephone: '' });
      loadDocteurs();
      setError('');
    } catch (err) {
      setError('Erreur lors de la modification du docteur');
      console.error(err);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce docteur ?')) {
      try {
        await docteurService.deleteDocteur(id);
        loadDocteurs();
        setError('');
      } catch (err) {
        setError('Erreur lors de la suppression du docteur');
        console.error(err);
      }
    }
  };

  const handleCancel = () => {
    setEditingId(null);
    setShowAddForm(false);
    setFormData({ nom: '', prenom: '', specialite: '', email: '', telephone: '' });
  };

  if (loading) return <div className="loading">Chargement...</div>;

  return (
    <div className="management-container">
      <div className="management-header">
        <h2>Gestion des Docteurs</h2>
        <button className="btn-primary" onClick={() => setShowAddForm(true)}>
          Ajouter un Docteur
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {showAddForm && (
        <div className="card form-card">
          <h3>Nouveau Docteur</h3>
          <form onSubmit={handleAdd}>
            <div className="form-group">
              <label>Nom:</label>
              <input
                type="text"
                name="nom"
                value={formData.nom}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Prénom:</label>
              <input
                type="text"
                name="prenom"
                value={formData.prenom}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Spécialité:</label>
              <input
                type="text"
                name="specialite"
                value={formData.specialite}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Email:</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Téléphone:</label>
              <input
                type="tel"
                name="telephone"
                value={formData.telephone}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn-primary">Créer</button>
              <button type="button" className="btn-secondary" onClick={handleCancel}>
                Annuler
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nom</th>
              <th>Prénom</th>
              <th>Spécialité</th>
              <th>Email</th>
              <th>Téléphone</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {docteurs.map((docteur) => (
              <tr key={docteur.id}>
                {editingId === docteur.id ? (
                  <>
                    <td>{docteur.id}</td>
                    <td>
                      <input
                        type="text"
                        name="nom"
                        value={formData.nom}
                        onChange={handleInputChange}
                      />
                    </td>
                    <td>
                      <input
                        type="text"
                        name="prenom"
                        value={formData.prenom}
                        onChange={handleInputChange}
                      />
                    </td>
                    <td>
                      <input
                        type="text"
                        name="specialite"
                        value={formData.specialite}
                        onChange={handleInputChange}
                      />
                    </td>
                    <td>
                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleInputChange}
                      />
                    </td>
                    <td>
                      <input
                        type="tel"
                        name="telephone"
                        value={formData.telephone}
                        onChange={handleInputChange}
                      />
                    </td>
                    <td>
                      <button className="btn-success btn-sm" onClick={handleUpdate}>
                        Enregistrer
                      </button>
                      <button className="btn-secondary btn-sm" onClick={handleCancel}>
                        Annuler
                      </button>
                    </td>
                  </>
                ) : (
                  <>
                    <td>{docteur.id}</td>
                    <td>{docteur.nom}</td>
                    <td>{docteur.prenom}</td>
                    <td>{docteur.specialite}</td>
                    <td>{docteur.email}</td>
                    <td>{docteur.telephone}</td>
                    <td>
                      <button
                        className="btn-warning btn-sm"
                        onClick={() => handleEdit(docteur)}
                      >
                        Modifier
                      </button>
                      <button
                        className="btn-danger btn-sm"
                        onClick={() => handleDelete(docteur.id)}
                      >
                        Supprimer
                      </button>
                    </td>
                  </>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default DocteurManagement;
