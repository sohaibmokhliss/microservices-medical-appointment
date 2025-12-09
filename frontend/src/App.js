import React, { useState } from 'react';
import './App.css';
import DocteurList from './components/DocteurList';
import RdvForm from './components/RdvForm';
import RdvList from './components/RdvList';

function App() {
  const [activeTab, setActiveTab] = useState('docteurs');

  return (
    <div className="App">
      <header className="App-header">
        <h1>Système de Prise de Rendez-vous Médicaux</h1>
      </header>

      <nav className="App-nav">
        <button
          className={activeTab === 'docteurs' ? 'active' : ''}
          onClick={() => setActiveTab('docteurs')}
        >
          Liste des Docteurs
        </button>
        <button
          className={activeTab === 'nouveau-rdv' ? 'active' : ''}
          onClick={() => setActiveTab('nouveau-rdv')}
        >
          Prendre Rendez-vous
        </button>
        <button
          className={activeTab === 'mes-rdv' ? 'active' : ''}
          onClick={() => setActiveTab('mes-rdv')}
        >
          Mes Rendez-vous
        </button>
      </nav>

      <main className="App-main">
        {activeTab === 'docteurs' && <DocteurList />}
        {activeTab === 'nouveau-rdv' && <RdvForm />}
        {activeTab === 'mes-rdv' && <RdvList />}
      </main>

      <footer className="App-footer">
        <p>Système de santé en ligne - 2025</p>
      </footer>
    </div>
  );
}

export default App;
