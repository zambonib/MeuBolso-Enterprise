import React, { useState, useEffect } from 'react';
import AccountModal from './AccountModal';
import CategoryModal from './CategoryModal';

/**
 * TransactionModal Component
 * @param {{
 *   isOpen: boolean,
 *   onClose: () => void,
 *   onSubmit: (formData: object) => Promise<void>,
 *   initialData?: object|null,
 *   accounts: Array<{ id: number, nome: string }>,
 *   categories: Array<{ id: number, nome: string, tipo: string }>,
 *   onRefreshAccounts?: () => Promise<void>|void,
 *   onRefreshCategories?: () => Promise<void>|void
 * }} props
 */
export const TransactionModal = ({
  isOpen,
  onClose,
  onSubmit,
  initialData = null,
  accounts = [],
  categories = [],
  onRefreshAccounts,
  onRefreshCategories,
}) => {
  const getTodayISO = () => new Date().toISOString().split('T')[0];

  const [formData, setFormData] = useState({
    descricao: '',
    valor: '',
    data: getTodayISO(),
    tipo: 'DESPESA',
    contaId: '',
    categoriaId: '',
  });

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [apiError, setApiError] = useState(null);

  const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    if (initialData) {
      setFormData({
        descricao: initialData.descricao || '',
        valor: initialData.valor ? String(initialData.valor) : '',
        data: initialData.data || getTodayISO(),
        tipo: initialData.tipo || 'DESPESA',
        contaId: initialData.contaId ? String(initialData.contaId) : '',
        categoriaId: initialData.categoriaId ? String(initialData.categoriaId) : '',
      });
    } else {
      setFormData({
        descricao: '',
        valor: '',
        data: getTodayISO(),
        tipo: 'DESPESA',
        contaId: accounts.length > 0 ? String(accounts[0].id) : '',
        categoriaId: '',
      });
    }
    setErrors({});
    setApiError(null);
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  // Filter categories by selected tipo
  const filteredCategories = categories.filter((c) => c.tipo === formData.tipo);

  const handleTipoChange = (newTipo) => {
    setFormData((prev) => ({
      ...prev,
      tipo: newTipo,
      categoriaId: '', // Reset category selection on type change
    }));
  };

  const handleAccountCreated = async (createdAccount) => {
    if (onRefreshAccounts) await onRefreshAccounts();
    if (createdAccount && createdAccount.id) {
      setFormData((prev) => ({ ...prev, contaId: String(createdAccount.id) }));
    }
  };

  const handleCategoryCreated = async (createdCategory) => {
    if (onRefreshCategories) await onRefreshCategories();
    if (createdCategory && createdCategory.id) {
      setFormData((prev) => ({
        ...prev,
        categoriaId: String(createdCategory.id),
        tipo: createdCategory.tipo || prev.tipo
      }));
    }
  };

  const validate = () => {
    const errs = {};
    if (!formData.descricao.trim()) {
      errs.descricao = 'A descrição é obrigatória (máx. 255 caracteres).';
    }
    const parsedVal = Number(formData.valor);
    if (!formData.valor || isNaN(parsedVal) || parsedVal <= 0) {
      errs.valor = 'O valor deve ser maior que zero.';
    }
    if (!formData.data) {
      errs.data = 'A data é obrigatória.';
    }
    if (!formData.contaId) {
      errs.contaId = 'Selecione uma conta.';
    }
    if (!formData.categoriaId) {
      errs.categoriaId = 'Selecione uma categoria.';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);

    if (!validate()) return;

    setSubmitting(true);
    try {
      const payload = {
        descricao: formData.descricao.trim(),
        valor: parseFloat(formData.valor),
        data: formData.data,
        tipo: formData.tipo,
        contaId: parseInt(formData.contaId, 10),
        categoriaId: parseInt(formData.categoriaId, 10),
      };

      await onSubmit(payload);
      onClose();
    } catch (err) {
      if (err && err.fieldErrors) {
        setErrors(err.fieldErrors);
      }
      setApiError(err.message || 'Erro ao salvar lançamento');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal-card" onClick={(e) => e.stopPropagation()}>
          <div className="modal-header">
            <h2 className="modal-title">
              {initialData ? 'Editar Transação' : 'Nova Transação'}
            </h2>
            <button type="button" className="btn btn-ghost btn-sm" onClick={onClose}>
              ✕
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              {apiError && <div className="alert-danger">{apiError}</div>}

              {/* Tipo Selector */}
              <div className="form-group">
                <label className="form-label">Tipo de Transação</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    type="button"
                    className={`btn ${formData.tipo === 'DESPESA' ? 'btn-danger' : 'btn-secondary'}`}
                    style={{ flex: 1 }}
                    onClick={() => handleTipoChange('DESPESA')}
                  >
                    - DESPESA
                  </button>
                  <button
                    type="button"
                    className={`btn ${formData.tipo === 'RECEITA' ? 'btn-primary' : 'btn-secondary'}`}
                    style={{ flex: 1, backgroundColor: formData.tipo === 'RECEITA' ? 'var(--nimbus-success)' : undefined }}
                    onClick={() => handleTipoChange('RECEITA')}
                  >
                    + RECEITA
                  </button>
                </div>
              </div>

              {/* Descrição */}
              <div className="form-group">
                <label className="form-label">Descrição</label>
                <input
                  type="text"
                  className={`form-input ${errors.descricao ? 'is-invalid' : ''}`}
                  placeholder="Ex: Compra de Supermercado"
                  value={formData.descricao}
                  onChange={(e) => setFormData({ ...formData, descricao: e.target.value })}
                />
                {errors.descricao && <span className="form-error">{errors.descricao}</span>}
              </div>

              {/* Valor & Data */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label className="form-label">Valor (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    className={`form-input ${errors.valor ? 'is-invalid' : ''}`}
                    placeholder="0.00"
                    value={formData.valor}
                    onChange={(e) => setFormData({ ...formData, valor: e.target.value })}
                  />
                  {errors.valor && <span className="form-error">{errors.valor}</span>}
                </div>

                <div className="form-group">
                  <label className="form-label">Data</label>
                  <input
                    type="date"
                    className={`form-input ${errors.data ? 'is-invalid' : ''}`}
                    value={formData.data}
                    onChange={(e) => setFormData({ ...formData, data: e.target.value })}
                  />
                  {errors.data && <span className="form-error">{errors.data}</span>}
                </div>
              </div>

              {/* Conta Select com botão + inline */}
              <div className="form-group">
                <label className="form-label">Conta Bancária</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <select
                    className={`form-select ${errors.contaId ? 'is-invalid' : ''}`}
                    value={formData.contaId}
                    onChange={(e) => setFormData({ ...formData, contaId: e.target.value })}
                    style={{ flex: 1 }}
                  >
                    <option value="">Selecione a conta...</option>
                    {accounts.map((acc) => (
                      <option key={acc.id} value={acc.id}>
                        {acc.nome} ({acc.tipo})
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setIsAccountModalOpen(true)}
                    title="Nova Conta"
                  >
                    ➕
                  </button>
                </div>
                {errors.contaId && <span className="form-error">{errors.contaId}</span>}
              </div>

              {/* Categoria Select com botão + inline */}
              <div className="form-group">
                <label className="form-label">Categoria ({formData.tipo})</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <select
                    className={`form-select ${errors.categoriaId ? 'is-invalid' : ''}`}
                    value={formData.categoriaId}
                    onChange={(e) => setFormData({ ...formData, categoriaId: e.target.value })}
                    style={{ flex: 1 }}
                  >
                    <option value="">Selecione a categoria...</option>
                    {filteredCategories.map((cat) => (
                      <option key={cat.id} value={cat.id}>
                        {cat.nome}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setIsCategoryModalOpen(true)}
                    title="Nova Categoria"
                  >
                    ➕
                  </button>
                </div>
                {errors.categoriaId && <span className="form-error">{errors.categoriaId}</span>}
              </div>
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={onClose}
                disabled={submitting}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                disabled={submitting}
              >
                {submitting ? 'Salvando...' : initialData ? 'Atualizar' : 'Salvar Transação'}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Sub-modais Inline */}
      <AccountModal
        isOpen={isAccountModalOpen}
        onClose={() => setIsAccountModalOpen(false)}
        onSuccess={handleAccountCreated}
      />

      <CategoryModal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        onSuccess={handleCategoryCreated}
        defaultTipo={formData.tipo}
      />
    </>
  );
};

export default TransactionModal;
