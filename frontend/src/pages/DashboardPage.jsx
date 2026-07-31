import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useAccounts } from '../hooks/useAccounts';
import { useCategories } from '../hooks/useCategories';
import { useTransactions } from '../hooks/useTransactions';
import StatCard from '../components/StatCard';
import TransactionTable from '../components/TransactionTable';
import TransactionModal from '../components/TransactionModal';
import AccountModal from '../components/AccountModal';
import CategoryModal from '../components/CategoryModal';
import Badge from '../components/Badge';

export const DashboardPage = () => {
  const { user } = useAuth();
  const { accounts, loading: loadingAccounts, refetch: refetchAccounts } = useAccounts();
  const { categories, loading: loadingCategories, refetch: refetchCategories } = useCategories();
  const { transactions, loading: loadingTransactions, refetch: refetchTransactions, createTransaction } = useTransactions();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);

  // Derived financial statistics
  const parseNumber = (val) => {
    if (val === null || val === undefined) return 0;
    const num = typeof val === 'number' ? val : parseFloat(val);
    return isNaN(num) ? 0 : num;
  };

  const totalSaldoInicial = accounts.reduce((acc, a) => acc + parseNumber(a.saldoInicial), 0);

  const totalReceitas = transactions
    .filter((t) => t.tipo === 'RECEITA')
    .reduce((acc, t) => acc + parseNumber(t.valor), 0);

  const totalDespesas = transactions
    .filter((t) => t.tipo === 'DESPESA')
    .reduce((acc, t) => acc + parseNumber(t.valor), 0);

  const balancoMensal = totalReceitas - totalDespesas;
  // O backend já atualiza o saldo da conta a cada transação, então o total do saldo das contas já é o saldo real atualizado
  const saldoTotalCalculado = totalSaldoInicial;

  // Recent 5 transactions
  const recentTransactions = [...transactions]
    .sort((a, b) => new Date(b.data || 0) - new Date(a.data || 0))
    .slice(0, 5);

  const handleCreateTransaction = async (formData) => {
    await createTransaction(formData);
    await refetchAccounts();
  };

  const loadingAll = loadingAccounts || loadingCategories || loadingTransactions;

  return (
    <div>
      {/* Header Toolbar */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Visão Geral</h1>
          <p className="page-subtitle">
            Olá, {user?.name || user?.username || 'Usuário'}! Acompanhe a saúde financeira da sua organização.
          </p>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setIsModalOpen(true)}
        >
          <span>➕</span> Nova Transação
        </button>
      </div>

      {/* 4 Stat Cards */}
      <div className="stats-grid">
        <StatCard
          title="Saldo Total"
          value={saldoTotalCalculado}
          icon="💰"
          variant="primary"
          loading={loadingAll}
        />
        <StatCard
          title="Receitas do Mês"
          value={totalReceitas}
          icon="📈"
          variant="success"
          loading={loadingAll}
        />
        <StatCard
          title="Despesas do Mês"
          value={totalDespesas}
          icon="📉"
          variant="danger"
          loading={loadingAll}
        />
        <StatCard
          title="Balanço Mensal"
          value={balancoMensal}
          icon="⚖️"
          variant={balancoMensal >= 0 ? 'success' : 'danger'}
          subtitle={balancoMensal >= 0 ? 'Resultado Positivo' : 'Resultado Negativo'}
          loading={loadingAll}
        />
      </div>

      {/* Grid: 8 Cols (Recent Transactions) + 4 Cols (Accounts & Categories) */}
      <div className="dashboard-content-grid">
        {/* Left Column */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="nimbus-card" style={{ padding: 0 }}>
            <div
              style={{
                padding: '1.25rem 1.5rem',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                borderBottom: '1px solid var(--nimbus-border)',
              }}
            >
              <h2 style={{ fontSize: '1.1rem' }}>Últimos Lançamentos</h2>
              <Link to="/transacoes" className="btn btn-ghost btn-sm">
                Ver Todos ➔
              </Link>
            </div>
            <TransactionTable
              transactions={recentTransactions}
              loading={loadingTransactions}
            />
          </div>
        </div>

        {/* Right Column */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Accounts Summary */}
          <div className="nimbus-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ fontSize: '1.1rem', margin: 0 }}>Minhas Contas</h2>
              <button className="btn btn-ghost btn-sm" onClick={() => setIsAccountModalOpen(true)}>➕ Nova</button>
            </div>
            {loadingAccounts ? (
              <p style={{ color: 'var(--nimbus-text-muted)', fontSize: '0.875rem' }}>Carregando contas...</p>
            ) : accounts.length === 0 ? (
              <p style={{ color: 'var(--nimbus-text-muted)', fontSize: '0.875rem' }}>Nenhuma conta cadastrada.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                {accounts.map((acc) => (
                  <div
                    key={acc.id}
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      padding: '0.65rem 0.85rem',
                      borderRadius: 'var(--nimbus-radius-md)',
                      backgroundColor: 'var(--nimbus-bg)',
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{acc.nome}</div>
                      <Badge variant="neutral">{acc.tipo}</Badge>
                    </div>
                    <div className="font-mono" style={{ fontWeight: 600 }}>
                      {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(acc.saldoInicial || 0)}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Categories Summary */}
          <div className="nimbus-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ fontSize: '1.1rem', margin: 0 }}>Categorias</h2>
              <button className="btn btn-ghost btn-sm" onClick={() => setIsCategoryModalOpen(true)}>➕ Nova</button>
            </div>
            {loadingCategories ? (
              <p style={{ color: 'var(--nimbus-text-muted)', fontSize: '0.875rem' }}>Carregando categorias...</p>
            ) : categories.length === 0 ? (
              <p style={{ color: 'var(--nimbus-text-muted)', fontSize: '0.875rem' }}>Nenhuma categoria cadastrada.</p>
            ) : (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                {categories.map((cat) => (
                  <Badge
                    key={cat.id}
                    variant={cat.tipo === 'RECEITA' ? 'success' : 'danger'}
                  >
                    {cat.nome}
                  </Badge>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Transaction Creation Modal */}
      <TransactionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateTransaction}
        accounts={accounts}
        categories={categories}
        onRefreshAccounts={refetchAccounts}
        onRefreshCategories={refetchCategories}
      />
      <AccountModal 
        isOpen={isAccountModalOpen} 
        onClose={() => setIsAccountModalOpen(false)} 
        onSuccess={refetchAccounts} 
      />
      <CategoryModal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        onSuccess={refetchCategories}
      />
    </div>
  );
};

export default DashboardPage;
