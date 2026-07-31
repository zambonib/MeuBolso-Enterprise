import React, { useState } from 'react';
import accountService from '../services/accountService';

export const AccountModal = ({ isOpen, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    nome: '',
    tipo: 'CORRENTE',
    saldoInicial: 0,
    numeroConta: '',
    chequeEspecial: 0,
    cor: 'linear-gradient(135deg, #2563eb, #7c3aed)'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const colorPresets = [
    { name: 'Meu Bolso (Azul/Roxo)', value: 'linear-gradient(135deg, #2563eb, #7c3aed)' },
    { name: 'Itaú (Laranja)', value: 'linear-gradient(135deg, #EC7000, #ff8c00)' },
    { name: 'Nubank (Roxo)', value: 'linear-gradient(135deg, #8A05BE, #5a037d)' },
    { name: 'Santander (Vermelho)', value: 'linear-gradient(135deg, #CC0000, #af0000)' },
    { name: 'Inter (Laranja Vivo)', value: 'linear-gradient(135deg, #FF7A00, #ff9e43)' },
    { name: 'Escuro (Grafite)', value: 'linear-gradient(135deg, #1e293b, #0f172a)' }
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const created = await accountService.create({
        ...formData,
        saldoInicial: parseFloat(formData.saldoInicial || 0),
        chequeEspecial: parseFloat(formData.chequeEspecial || 0)
      });
      setFormData({
        nome: '',
        tipo: 'CORRENTE',
        saldoInicial: 0,
        numeroConta: '',
        chequeEspecial: 0,
        cor: 'linear-gradient(135deg, #2563eb, #7c3aed)'
      });
      if (onSuccess) onSuccess(created);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao criar conta');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay-ui active" style={{ zIndex: 60 }}>
      <div className="modal-box floating-box">
        <div className="modal-header">
          <h2>Nova Conta Bancária</h2>
          <button type="button" className="close-btn" onClick={onClose}>✖</button>
        </div>
        
        {error && <div className="alert alert-danger" style={{marginBottom: '1rem', color: 'red'}}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group" style={{ marginBottom: '1rem' }}>
            <label>Nome da Conta / Banco</label>
            <input 
              type="text" 
              className="form-control"
              placeholder="Ex: Itaú, Nubank, Banco do Brasil..." 
              value={formData.nome}
              onChange={(e) => setFormData({...formData, nome: e.target.value})}
              required
              autoFocus
            />
          </div>

          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
            <div className="form-group">
              <label>Tipo de Conta</label>
              <select 
                className="form-control"
                value={formData.tipo}
                onChange={(e) => setFormData({...formData, tipo: e.target.value})}
              >
                <option value="CORRENTE">Conta Corrente</option>
                <option value="POUPANCA">Poupança</option>
                <option value="INVESTIMENTO">Investimento</option>
                <option value="CARTAO">Cartão de Crédito</option>
              </select>
            </div>
            
            <div className="form-group">
              <label>Número da Conta</label>
              <input 
                type="text" 
                className="form-control"
                placeholder="Ex: 12345-6"
                value={formData.numeroConta}
                onChange={(e) => setFormData({...formData, numeroConta: e.target.value})}
              />
            </div>
          </div>

          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
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

            <div className="form-group">
              <label>Cheque Especial (R$)</label>
              <input 
                type="number" 
                step="0.01" 
                className="form-control"
                placeholder="0,00"
                value={formData.chequeEspecial}
                onChange={(e) => setFormData({...formData, chequeEspecial: e.target.value})}
              />
            </div>
          </div>

          <div className="form-group" style={{ marginBottom: '1.5rem' }}>
            <label>Cor do Cartão do Banco</label>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginTop: '0.25rem' }}>
              {colorPresets.map((p) => (
                <button
                  key={p.value}
                  type="button"
                  onClick={() => setFormData({ ...formData, cor: p.value })}
                  style={{
                    background: p.value,
                    width: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    border: formData.cor === p.value ? '3px solid var(--nimbus-text-heading)' : 'none',
                    cursor: 'pointer',
                    boxShadow: '0 2px 6px rgba(0,0,0,0.2)'
                  }}
                  title={p.name}
                />
              ))}
            </div>
          </div>

          <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
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
