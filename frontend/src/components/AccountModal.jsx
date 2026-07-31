import React, { useState } from 'react';
import accountService from '../services/accountService';

export const AccountModal = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    nome: '',
    tipo: 'CORRENTE',
    saldoInicial: 0
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await accountService.create({
        ...formData,
        saldoInicial: parseFloat(formData.saldoInicial)
      });
      setFormData({ nome: '', tipo: 'CORRENTE', saldoInicial: 0 });
      if (onSuccess) onSuccess();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao criar conta');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay-ui active">
      <div className="modal-box floating-box">
        <div className="modal-header">
          <h2>Nova Conta Bancária</h2>
          <button type="button" className="close-btn" onClick={onClose}>✖</button>
        </div>
        
        {error && <div className="alert alert-danger" style={{marginBottom: '1rem', color: 'red'}}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group" style={{ marginBottom: '1rem' }}>
            <label>Nome da Conta</label>
            <input 
              type="text" 
              className="form-control"
              placeholder="Ex: Itaú, Nubank..." 
              value={formData.nome}
              onChange={(e) => setFormData({...formData, nome: e.target.value})}
              required
            />
          </div>

          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
            <div className="form-group">
              <label>Tipo</label>
              <select 
                className="form-control"
                value={formData.tipo}
                onChange={(e) => setFormData({...formData, tipo: e.target.value})}
              >
                <option value="CORRENTE">Corrente</option>
                <option value="POUPANCA">Poupança</option>
                <option value="INVESTIMENTO">Investimento</option>
                <option value="CARTAO">Cartão de Crédito</option>
              </select>
            </div>
            
            <div className="form-group">
              <label>Saldo Inicial (R$)</label>
              <input 
                type="number" 
                step="0.01" 
                className="form-control"
                placeholder="0,00"
                value={formData.saldoInicial}
                onChange={(e) => setFormData({...formData, saldoInicial: e.target.value})}
              />
            </div>
          </div>

          <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
            <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Salvando...' : 'Salvar Conta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AccountModal;
