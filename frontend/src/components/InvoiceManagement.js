import React, { useState, useEffect } from 'react';
import { billingService, rdvService, docteurService } from '../services/api';

function InvoiceManagement() {
  const [invoices, setInvoices] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [paymentData, setPaymentData] = useState({
    amount: '',
    paymentMethod: 'CASH'
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);

      // Load invoices
      const invoicesResponse = await billingService.getAllInvoices();
      setInvoices(invoicesResponse.data);

      // Load appointments
      const rdvResponse = await rdvService.getAllRdv();
      const allAppointments = rdvResponse.data;

      // Load doctors
      const doctorsResponse = await docteurService.getAllDocteurs();
      const doctorsMap = {};
      doctorsResponse.data.forEach(doc => {
        doctorsMap[doc.id] = doc;
      });
      setDoctors(doctorsMap);

      // Filter appointments that don't have invoices yet
      const invoicedRdvIds = invoicesResponse.data.map(inv => inv.rdvId);
      const uninvoicedAppointments = allAppointments.filter(rdv =>
        !invoicedRdvIds.includes(rdv.id) && rdv.statut === 'CONFIRMÉ'
      );
      setAppointments(uninvoicedAppointments);

      setError('');
    } catch (err) {
      setError('Erreur lors du chargement des données');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handlePaymentInputChange = (e) => {
    setPaymentData({
      ...paymentData,
      [e.target.name]: e.target.value
    });
  };

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    if (!selectedInvoice) return;

    try {
      const payment = {
        invoiceId: selectedInvoice.id,
        amount: parseFloat(paymentData.amount),
        paymentMethod: paymentData.paymentMethod,
        status: 'SUCCESS'
      };

      await billingService.recordPayment(payment);
      setShowPaymentForm(false);
      setSelectedInvoice(null);
      setPaymentData({ amount: '', paymentMethod: 'CASH' });
      loadData();
      setError('');
    } catch (err) {
      setError('Erreur lors de l\'enregistrement du paiement');
      console.error(err);
    }
  };

  const [customAmount, setCustomAmount] = useState('');

  const openCreateInvoiceForm = (appointment) => {
    setSelectedAppointment(appointment);
    setCustomAmount('');
    setShowCreateForm(true);
    setShowPaymentForm(false);
  };

  const handleCreateInvoice = async () => {
    if (!selectedAppointment) return;

    try {
      const doctor = doctors[selectedAppointment.docteurId];
      const invoice = {
        rdvId: selectedAppointment.id,
        patientEmail: selectedAppointment.patientEmail,
        patientName: `${selectedAppointment.patientPrenom} ${selectedAppointment.patientNom}`,
        doctorName: doctor ? `Dr. ${doctor.prenom} ${doctor.nom}` : 'N/A',
        specialty: doctor ? doctor.specialite : '',
        description: `Consultation - ${selectedAppointment.motif}`
      };

      // Add custom amount if provided
      if (customAmount && parseFloat(customAmount) > 0) {
        invoice.amount = parseFloat(customAmount);
        invoice.tax = 0;
        invoice.total = parseFloat(customAmount);
      }

      await billingService.createInvoice(invoice);
      setShowCreateForm(false);
      setSelectedAppointment(null);
      setCustomAmount('');
      loadData();
      setError('');
    } catch (err) {
      setError('Erreur lors de la création de la facture');
      console.error(err);
    }
  };

  const openPaymentForm = (invoice) => {
    setSelectedInvoice(invoice);
    setPaymentData({ ...paymentData, amount: invoice.total.toString() });
    setShowPaymentForm(true);
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'PAID':
        return 'status-badge active';
      case 'PENDING':
        return 'status-badge inactive';
      case 'PARTIALLY_PAID':
        return 'status-badge';
      case 'OVERDUE':
        return 'status-badge inactive';
      default:
        return 'status-badge';
    }
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case 'PAID':
        return 'Payé';
      case 'PENDING':
        return 'En attente';
      case 'PARTIALLY_PAID':
        return 'Partiellement payé';
      case 'OVERDUE':
        return 'En retard';
      default:
        return status;
    }
  };

  if (loading) return <div className="loading">Chargement...</div>;

  return (
    <div className="management-container">
      <div className="management-header">
        <h2>Gestion des Factures et Paiements</h2>
      </div>

      {error && <div className="error-message">{error}</div>}

      {showCreateForm && selectedAppointment && (
        <div className="card form-card">
          <h3>Créer une Facture</h3>
          <div className="invoice-details">
            <p><strong>Rendez-vous ID:</strong> #{selectedAppointment.id}</p>
            <p><strong>Patient:</strong> {selectedAppointment.patientPrenom} {selectedAppointment.patientNom}</p>
            <p><strong>Email:</strong> {selectedAppointment.patientEmail}</p>
            <p><strong>Médecin:</strong> {doctors[selectedAppointment.docteurId] ?
              `Dr. ${doctors[selectedAppointment.docteurId].prenom} ${doctors[selectedAppointment.docteurId].nom}` : 'N/A'}</p>
            <p><strong>Spécialité:</strong> {doctors[selectedAppointment.docteurId]?.specialite || 'N/A'}</p>
            <p><strong>Motif:</strong> {selectedAppointment.motif}</p>
            <p><strong>Date:</strong> {new Date(selectedAppointment.dateHeure).toLocaleString('fr-FR')}</p>
          </div>
          <div className="form-group" style={{ marginTop: '20px' }}>
            <label>Montant personnalisé (MAD) - optionnel:</label>
            <input
              type="number"
              value={customAmount}
              onChange={(e) => setCustomAmount(e.target.value)}
              placeholder="Laisser vide pour le montant par défaut (300 MAD)"
              step="0.01"
              min="0"
              style={{ width: '100%' }}
            />
            <small style={{ color: '#666', fontSize: '0.9em' }}>
              Si vous ne spécifiez pas de montant, le prix par défaut sera utilisé (300 MAD pour Cardiologie)
            </small>
          </div>
          <div className="form-actions">
            <button className="btn-primary" onClick={handleCreateInvoice}>
              Créer la facture
            </button>
            <button
              className="btn-secondary"
              onClick={() => {
                setShowCreateForm(false);
                setSelectedAppointment(null);
                setCustomAmount('');
              }}
            >
              Annuler
            </button>
          </div>
        </div>
      )}

      {appointments.length > 0 && !showCreateForm && (
        <div className="card" style={{ marginBottom: '20px' }}>
          <h3>Rendez-vous à Facturer</h3>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Patient</th>
                  <th>Email</th>
                  <th>Médecin</th>
                  <th>Spécialité</th>
                  <th>Date</th>
                  <th>Motif</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((rdv) => {
                  const doctor = doctors[rdv.docteurId];
                  return (
                    <tr key={rdv.id}>
                      <td>#{rdv.id}</td>
                      <td>{rdv.patientPrenom} {rdv.patientNom}</td>
                      <td>{rdv.patientEmail}</td>
                      <td>{doctor ? `Dr. ${doctor.prenom} ${doctor.nom}` : 'N/A'}</td>
                      <td>{doctor?.specialite || 'N/A'}</td>
                      <td>{new Date(rdv.dateHeure).toLocaleString('fr-FR')}</td>
                      <td>{rdv.motif}</td>
                      <td>
                        <button
                          className="btn-primary btn-sm"
                          onClick={() => openCreateInvoiceForm(rdv)}
                        >
                          Créer facture
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showPaymentForm && selectedInvoice && (
        <div className="card form-card">
          <h3>Enregistrer un Paiement</h3>
          <div className="invoice-details">
            <p><strong>Facture:</strong> #{selectedInvoice.id}</p>
            <p><strong>Patient:</strong> {selectedInvoice.patientName}</p>
            <p><strong>Montant total:</strong> {selectedInvoice.total} MAD</p>
          </div>
          <form onSubmit={handleRecordPayment}>
            <div className="form-group">
              <label>Montant du paiement (MAD):</label>
              <input
                type="number"
                name="amount"
                value={paymentData.amount}
                onChange={handlePaymentInputChange}
                step="0.01"
                min="0.01"
                max={selectedInvoice.total}
                required
              />
            </div>
            <div className="form-group">
              <label>Méthode de paiement:</label>
              <select
                name="paymentMethod"
                value={paymentData.paymentMethod}
                onChange={handlePaymentInputChange}
                required
              >
                <option value="CASH">Espèces</option>
                <option value="CARD">Carte bancaire</option>
                <option value="BANK_TRANSFER">Virement bancaire</option>
                <option value="ONLINE">Paiement en ligne</option>
              </select>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn-primary">Enregistrer le paiement</button>
              <button
                type="button"
                className="btn-secondary"
                onClick={() => {
                  setShowPaymentForm(false);
                  setSelectedInvoice(null);
                }}
              >
                Annuler
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="card">
        <h3>Factures Existantes</h3>
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Patient</th>
                <th>Email</th>
                <th>Médecin</th>
                <th>Spécialité</th>
                <th>Montant</th>
                <th>Statut</th>
                <th>Date création</th>
                <th>Actions</th>
              </tr>
            </thead>
          <tbody>
            {invoices.map((invoice) => (
              <tr key={invoice.id}>
                <td>#{invoice.id}</td>
                <td>{invoice.patientName}</td>
                <td>{invoice.patientEmail}</td>
                <td>{invoice.doctorName}</td>
                <td>{invoice.specialty}</td>
                <td>{invoice.total} MAD</td>
                <td>
                  <span className={getStatusBadgeClass(invoice.status)}>
                    {getStatusLabel(invoice.status)}
                  </span>
                </td>
                <td>{new Date(invoice.createdDate).toLocaleDateString('fr-FR')}</td>
                <td>
                  {invoice.status !== 'PAID' && (
                    <button
                      className="btn-success btn-sm"
                      onClick={() => openPaymentForm(invoice)}
                    >
                      Enregistrer paiement
                    </button>
                  )}
                  {invoice.status === 'PAID' && (
                    <span style={{ color: '#28a745', fontWeight: 'bold' }}>✓ Payé</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        </div>
      </div>
    </div>
  );
}

export default InvoiceManagement;
