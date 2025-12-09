import React, { useEffect, useState } from 'react';
import './App.css';
import DocteurList from './components/DocteurList';
import RdvForm from './components/RdvForm';
import RdvList from './components/RdvList';
import AuthPanel from './components/AuthPanel';
import DocteurManagement from './components/DocteurManagement';
import UserManagement from './components/UserManagement';
import { authService } from './services/auth';
import { TOKEN_KEY, UNAUTHORIZED_EVENT } from './services/apiClient';

function App() {
  const [activeTab, setActiveTab] = useState('docteurs');
  const [token, setToken] = useState('');
  const [user, setUser] = useState(null);
  const [authStatus, setAuthStatus] = useState({ loading: false, error: '' });

  useEffect(() => {
    const storedToken = localStorage.getItem(TOKEN_KEY);
    if (storedToken) {
      setToken(storedToken);
    }

    const handleUnauthorized = () => {
      handleLogout();
    };

    window.addEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
  }, []);

  useEffect(() => {
    const loadCurrentUser = async () => {
      setAuthStatus({ loading: true, error: '' });
      try {
        const { data } = await authService.getCurrentUser();
        setUser(data);
        setAuthStatus({ loading: false, error: '' });
      } catch (err) {
        console.error('Erreur lors du chargement de l’utilisateur', err);
        setUser(null);
        setToken('');
        localStorage.removeItem(TOKEN_KEY);
        setAuthStatus({
          loading: false,
          error: 'Session expirée ou non authentifiée. Veuillez vous reconnecter.'
        });
      }
    };

    if (token) {
      loadCurrentUser();
    }
  }, [token]);

  const handleAuthenticated = (newToken) => {
    localStorage.setItem(TOKEN_KEY, newToken);
    setToken(newToken);
    setAuthStatus({ loading: false, error: '' });
  };

  const handleLogout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setToken('');
    setUser(null);
    setActiveTab('docteurs');
    setAuthStatus({ loading: false, error: '' });
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Système de Prise de Rendez-vous Médicaux</h1>
      </header>

      <main className="App-main">
        <AuthPanel
          user={user}
          loading={authStatus.loading}
          error={authStatus.error}
          onAuthenticated={handleAuthenticated}
          onLogout={handleLogout}
        />

        <nav className="App-nav">
          <button
            className={activeTab === 'docteurs' ? 'active' : ''}
            onClick={() => setActiveTab('docteurs')}
            disabled={!user}
          >
            Liste des Docteurs
          </button>
          <button
            className={activeTab === 'nouveau-rdv' ? 'active' : ''}
            onClick={() => setActiveTab('nouveau-rdv')}
            disabled={!user}
          >
            Prendre Rendez-vous
          </button>
          <button
            className={activeTab === 'mes-rdv' ? 'active' : ''}
            onClick={() => setActiveTab('mes-rdv')}
            disabled={!user}
          >
            Mes Rendez-vous
          </button>
          {user?.role === 'ADMIN' && (
            <>
              <button
                className={activeTab === 'gestion-docteurs' ? 'active' : ''}
                onClick={() => setActiveTab('gestion-docteurs')}
              >
                Gestion Docteurs
              </button>
              <button
                className={activeTab === 'gestion-users' ? 'active' : ''}
                onClick={() => setActiveTab('gestion-users')}
              >
                Gestion Réceptionnistes
              </button>
            </>
          )}
        </nav>

        {user ? (
          <>
            {activeTab === 'docteurs' && <DocteurList />}
            {activeTab === 'nouveau-rdv' && <RdvForm />}
            {activeTab === 'mes-rdv' && <RdvList />}
            {activeTab === 'gestion-docteurs' && user.role === 'ADMIN' && <DocteurManagement />}
            {activeTab === 'gestion-users' && user.role === 'ADMIN' && <UserManagement />}
          </>
        ) : (
          <div className="card info-card">
            <h2>Connexion requise</h2>
            <p>Créez un compte ou connectez-vous pour accéder aux listes de docteurs et gérer vos rendez-vous.</p>
          </div>
        )}
      </main>

      <footer className="App-footer">
        <p>Système de santé en ligne - 2025</p>
      </footer>
    </div>
  );
}

export default App;
