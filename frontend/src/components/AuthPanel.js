import React, { useState } from 'react';
import { authService } from '../services/auth';

function AuthPanel({ user, loading, error, onAuthenticated, onLogout }) {
  const [form, setForm] = useState({
    username: '',
    password: ''
  });
  const [status, setStatus] = useState({ message: '', type: '' });
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setStatus({ message: '', type: '' });
    setSubmitting(true);

    try {
      const payload = { username: form.username, password: form.password };
      const { data } = await authService.login(payload);

      if (data.token) {
        onAuthenticated(data.token);
      }

      setStatus({
        message: data.message || 'Authentification réussie.',
        type: 'success'
      });
    } catch (err) {
      const serverMessage =
        err.response?.data?.message ||
        'Impossible de valider vos informations. Veuillez réessayer.';
      setStatus({ message: serverMessage, type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  if (user) {
    return (
      <div className="card auth-card">
        <div>
          <p className="auth-eyebrow">Connecté</p>
          <h3>{user.username}</h3>
          <p className="muted">Rôle: {user.role}</p>
          {user.email && <p className="muted">{user.email}</p>}
        </div>
        <div className="auth-actions">
          {loading && <span className="badge">Vérification...</span>}
          {error && <span className="badge error-badge">{error}</span>}
          <button className="btn btn-secondary" onClick={onLogout}>
            Se déconnecter
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="card auth-card">
      {error && <div className="error">{error}</div>}
      {status.message && (
        <div className={status.type === 'success' ? 'success' : 'error'}>
          {status.message}
        </div>
      )}

      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="username">Nom d&apos;utilisateur</label>
            <input
              id="username"
              name="username"
              value={form.username}
              onChange={handleChange}
              required
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mot de passe</label>
            <input
              type="password"
              id="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              required
              autoComplete="current-password"
            />
          </div>
        </div>

        <button className="btn btn-primary" type="submit" disabled={loading || submitting}>
          {submitting ? 'Connexion...' : 'Se connecter'}
        </button>
      </form>
    </div>
  );
}

export default AuthPanel;
