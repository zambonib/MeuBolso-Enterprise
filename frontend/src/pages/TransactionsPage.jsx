import React, { useState, useMemo } from 'react';
import { useTransactions } from '../hooks/useTransactions';
import { useAccounts } from '../hooks/useAccounts';
import { useCategories } from '../hooks/useCategories';
import TransactionTable from '../components/TransactionTable';
import TransactionModal from '../components/TransactionModal';

export const TransactionsPage = () => {
  const { accounts, refetch: refetchAccounts } = useAccounts();
  const { categories } = useCategories();
  const {
    transactions,
    filters,
    setFilters,
    loading: loadingTransactions,
    createTransaction,
    updateTransaction,
    deleteTransaction,
  } = useTransactions();

  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);

  const handleOpenCreateModal = () => {
    setEditingTransaction(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (tx) => {
    setEditingTransaction(tx);
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Tem certeza que deseja excluir esta transação?')) {
      try {
        await deleteTransaction(id);
        await refetchAccounts();
      } catch (err) {
        alert(err.message || 'Erro ao excluir transação.');
      }
    }
  };

  const handleSaveTransaction = async (formData) => {
    if (editingTransaction) {
      await updateTransaction(editingTransaction.id, formData);
    } else {
      await createTransaction(formData);
    }
    await refetchAccounts();
  };

  const clearFilters = () => {
    setFilters({});
    setSearchTerm('');
  };

  // Client-side text search filtering on top of server filters
  const filteredTransactions = useMemo(() => {
    if (!searchTerm.trim()) return transactions;
    const lower = searchTerm.toLowerCase();
    return transactions.filter((t) =>
      t.descricao ? t.descricao.toLowerCase().includes(lower) : false
    );
  }, [transactions, searchTerm]);

  return (
    <div>
      {/* Header Toolbar */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Lançamentos</h1>
          <p className="page-subtitle">Gerencie suas receitas e despesas.</p>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={handleOpenCreateModal}
        >
          <span>➕</span> Nova Transação
        </button>
      </div>

      {/* Filter Bar */}
      <div className="filters-bar">
        {/* Search */}
        <div style={{ flex: 1, minWidth: '200px' }}>
          <input
            type="text"
            className="form-input"
            placeholder="🔍 Buscar por descrição..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        {/* Tipo Filter */}
        <select
          className="form-select"
          style={{ width: 'auto', minWidth: '130px' }}
          value={filters.tipo || ''}
          onChange={(e) => setFilters({ ...filters, tipo: e.target.value || undefined })}
        >
          <option value="">Todos os Tipos</option>
          <option value="RECEITA">RECEITA</option>
          <option value="DESPESA">DESPESA</option>
        </select>

        {/* Conta Filter */}
        <select
          className="form-select"
          style={{ width: 'auto', minWidth: '150px' }}
          value={filters.contaId || ''}
          onChange={(e) => setFilters({ ...filters, contaId: e.target.value || undefined })}
        >
          <option value="">Todas as Contas</option>
          {accounts.map((acc) => (
            <option key={acc.id} value={acc.id}>
              {acc.nome}
            </option>
          ))}
        </select>

        {/* Categoria Filter */}
        <select
          className="form-select"
          style={{ width: 'auto', minWidth: '160px' }}
          value={filters.categoriaId || ''}
          onChange={(e) => setFilters({ ...filters, categoriaId: e.target.value || undefined })}
        >
          <option value="">Todas as Categorias</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.nome} ({cat.tipo})
            </option>
          ))}
        </select>

        {/* Clear Filters CTA */}
        {(searchTerm || filters.tipo || filters.contaId || filters.categoriaId) && (
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={clearFilters}
          >
            Limpar Filtros
          </button>
        )}
      </div>

      {/* Main Transactions Table */}
      <TransactionTable
        transactions={filteredTransactions}
        loading={loadingTransactions}
        onEdit={handleOpenEditModal}
        onDelete={handleDelete}
      />

      {/* Modal Dialog */}
      <TransactionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSaveTransaction}
        initialData={editingTransaction}
        accounts={accounts}
        categories={categories}
      />
    </div>
  );
};

export default TransactionsPage;
