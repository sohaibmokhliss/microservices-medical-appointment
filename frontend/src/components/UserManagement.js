import React, { useState, useEffect } from 'react';
import { userService } from '../services/api';

function UserManagement() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    role: 'RECEPTIONIST',
    enabled: true
  });

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await userService.getAllUsers();
      setUsers(response.data);
      setError('');
    } catch (err) {
      setError('Erreur lors du chargement des utilisateurs');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setFormData({
      ...formData,
      [e.target.name]: value
    });
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    try {
      await userService.createUser(formData);
      setFormData({ username: '', password: '', email: '', role: 'RECEPTIONIST', enabled: true });
      setShowAddForm(false);
      loadUsers();
      setError('');
    } catch (err) {
      setError(err.response?.data?.error || 'Erreur lors de la création de l\'utilisateur');
      console.error(err);
    }
  };

  const handleEdit = (user) => {
    setEditingId(user.id);
    setFormData({
      username: user.username,
      password: '',
      email: user.email,
      role: user.role,
      enabled: user.enabled
    });
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      const updates = { ...formData };
      if (!updates.password) {
        delete updates.password;
      }
      await userService.updateUser(editingId, updates);
      setEditingId(null);
      setFormData({ username: '', password: '', email: '', role: 'DOCTOR', enabled: true });
      loadUsers();
      setError('');
    } catch (err) {
      setError(err.response?.data?.error || 'Erreur lors de la modification de l\'utilisateur');
      console.error(err);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      try {
        await userService.deleteUser(id);
        loadUsers();
        setError('');
      } catch (err) {
        setError('Erreur lors de la suppression de l\'utilisateur');
        console.error(err);
      }
    }
  };


  const handleCancel = () => {
    setEditingId(null);
    setShowAddForm(false);
    setFormData({ username: '', password: '', email: '', role: 'RECEPTIONIST', enabled: true });
  };

  if (loading) return <div className="loading">Chargement...</div>;

  return (
    <div className="management-container">
      <div className="management-header">
        <h2>Gestion des Réceptionnistes</h2>
        <button className="btn-primary" onClick={() => setShowAddForm(true)}>
          Ajouter un Réceptionniste
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {showAddForm && (
        <div className="card form-card">
          <h3>Nouvel Utilisateur Réceptionniste</h3>
          <form onSubmit={handleAdd}>
            <div className="form-group">
              <label>Nom d'utilisateur:</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                required
                minLength={3}
              />
            </div>
            <div className="form-group">
              <label>Mot de passe:</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                required
                minLength={6}
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
            <input type="hidden" name="role" value="RECEPTIONIST" />
            <div className="form-actions">
              <button type="submit" className="btn-primary">Créer Réceptionniste</button>
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
              <th>Nom d'utilisateur</th>
              <th>Email</th>
              <th>Rôle</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                {editingId === user.id ? (
                  <>
                    <td>{user.id}</td>
                    <td>
                      <input
                        type="text"
                        name="username"
                        value={formData.username}
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
                      {user.role === 'ADMIN' ? (
                        <span className="badge">ADMIN</span>
                      ) : (
                        <span>{user.role}</span>
                      )}
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
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>{user.email}</td>
                    <td>{user.role}</td>
                    <td>
                      <button
                        className="btn-warning btn-sm"
                        onClick={() => handleEdit(user)}
                      >
                        Modifier
                      </button>
                      <button
                        className="btn-danger btn-sm"
                        onClick={() => handleDelete(user.id)}
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

export default UserManagement;
