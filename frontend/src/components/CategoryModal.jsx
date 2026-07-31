import React, { useState } from 'react';
import categoryService from '../services/categoryService';

export const CategoryModal = ({ isOpen, onClose, onSuccess, defaultTipo = 'DESPESA' }) => {
  const [nome, setNome] = useState('');
  const [tipo, setTipo] = useState(defaultTipo);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const created = await categoryService.create({
        nome: nome.trim(),
        tipo
      });
      setNome('');
      if (onSuccess) onSuccess(created);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao criar categoria');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay-ui active" style={{ zIndex: 60 }}>
      <div className="modal-box floating-box">
        <div className="modal-header">
          <h2>Nova Categoria</h2>
          <button type="button" className="close-btn" onClick={onClose}>✖</button>
        </div>

        {error && <div className="alert alert-danger" style={{ marginBottom: '1rem', color: 'red' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group" style={{ marginBottom: '1rem' }}>
            <label>Nome da Categoria</label>
            <input
              type="text"
              className="form-control"
              placeholder="Ex: Alimentação, Mercado, Bônus..."
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              required
              autoFocus
            />
          </div>

          <div className="form-group" style={{ marginBottom: '1.5rem' }}>
            <label>Tipo da Categoria</label>
            <select
              className="form-control"
              value={tipo}
              onChange={(e) => setTipo(e.target.value)}
            >
              <option value="DESPESA">Despesa</option>
              <option value="RECEITA">Receita</option>
            </select>
          </div>

          <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
            <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Salvando...' : 'Salvar Categoria'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CategoryModal;
