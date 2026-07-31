import React, { useState } from 'react';
import { useAccounts } from '../hooks/useAccounts';
import { useTransactions } from '../hooks/useTransactions';
import StatCard from '../components/StatCard';
import TransactionTable from '../components/TransactionTable';
import AccountModal from '../components/AccountModal';

export const AccountsPage = () => {
  const { accounts, loading: loadingAccounts, refetch: refetchAccounts } = useAccounts();
  const { transactions, loading: loadingTransactions } = useTransactions();

  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const parseNumber = (val) => {
    if (val === null || val === undefined) return 0;
    const num = typeof val === 'number' ? val : parseFloat(val);
    return isNaN(num) ? 0 : num;
  };

  const totalSaldo = accounts.reduce((acc, a) => acc + parseNumber(a.saldoInicial), 0);
  const totalChequeEspecial = accounts.reduce((acc, a) => acc + parseNumber(a.chequeEspecial), 0);

  // Maximum balance for chart bar scaling
  const maxSaldo = Math.max(...accounts.map((a) => Math.abs(parseNumber(a.saldoInicial))), 100);

  // Filter transactions for the selected card
  const filteredTransactions = selectedAccountId
    ? transactions.filter((t) => Number(t.contaId) === Number(selectedAccountId))
    : transactions;

  const selectedAccountObj = accounts.find((a) => Number(a.id) === Number(selectedAccountId));

  return (
    <div className="accounts-page-wrapper">
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Contas Bancárias</h1>
          <p className="page-subtitle">
            Gerencie seus bancos, cartões e acompanhe saldos e lançamentos por conta.
          </p>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setIsModalOpen(true)}
        >
          <span>➕</span> Nova Conta
        </button>
      </div>

      {/* Overview Stat & Bar Chart */}
      <div className="dashboard-content-grid" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div className="stats-grid" style={{ marginBottom: 0 }}>
            <StatCard
              title="Saldo Consolidado Total"
              value={totalSaldo}
              icon="💳"
              variant="primary"
              loading={loadingAccounts}
            />
            <StatCard
              title="Total Cheque Especial Disponível"
              value={totalChequeEspecial}
              icon="🛡️"
              variant="info"
              loading={loadingAccounts}
            />
          </div>
        </div>

        {/* Visual Balance Comparison Chart */}
        <div className="nimbus-card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <h3 style={{ fontSize: '0.95rem', marginBottom: '1rem', color: 'var(--nimbus-text-muted)' }}>
            📊 Comparativo de Saldos
          </h3>
          {loadingAccounts ? (
            <p style={{ fontSize: '0.85rem', color: 'var(--nimbus-text-muted)' }}>Carregando gráfico...</p>
          ) : accounts.length === 0 ? (
            <p style={{ fontSize: '0.85rem', color: 'var(--nimbus-text-muted)' }}>Nenhuma conta cadastrada.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {accounts.map((acc) => {
                const val = parseNumber(acc.saldoInicial);
                const percent = Math.min(Math.max((Math.abs(val) / maxSaldo) * 100, 8), 100);
                return (
                  <div key={acc.id} style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.825rem', fontWeight: 600 }}>
                      <span>{acc.nome}</span>
                      <span className="font-mono">
                        {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val)}
                      </span>
                    </div>
                    <div style={{ width: '100%', height: '8px', background: 'var(--nimbus-surface-hover)', borderRadius: '999px', overflow: 'hidden' }}>
                      <div
                        style={{
                          height: '100%',
                          width: `${percent}%`,
                          background: acc.cor || 'linear-gradient(90deg, #2563eb, #7c3aed)',
                          borderRadius: '999px',
                          transition: 'width 0.5s ease'
                        }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Bank Debit Cards Section */}
      <div style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.2rem' }}>Seus Cartões e Contas</h2>
          {selectedAccountId && (
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() => setSelectedAccountId(null)}
            >
              🔄 Mostrar Todas as Contas
            </button>
          )}
        </div>

        {loadingAccounts ? (
          <p style={{ color: 'var(--nimbus-text-muted)' }}>Carregando contas...</p>
        ) : accounts.length === 0 ? (
          <div className="nimbus-card" style={{ textAlign: 'center', padding: '3rem' }}>
            <p style={{ color: 'var(--nimbus-text-muted)', marginBottom: '1rem' }}>Você ainda não cadastrou nenhuma conta bancária.</p>
            <button type="button" className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
              Cadastrar Primeira Conta
            </button>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.5rem' }}>
            {accounts.map((acc) => {
              const isSelected = Number(acc.id) === Number(selectedAccountId);
              return (
                <div
                  key={acc.id}
                  className={`bank-card ${isSelected ? 'selected' : ''}`}
                  style={{ background: acc.cor || 'linear-gradient(135deg, #2563eb, #7c3aed)' }}
                  onClick={() => setSelectedAccountId(isSelected ? null : acc.id)}
                >
                  <div className="bank-card-header">
                    <span className="bank-card-brand">{acc.nome}</span>
                    <div className="bank-card-chip" />
                  </div>

                  <div className="bank-card-number">
                    {acc.numeroConta ? acc.numeroConta : `•••• •••• ${String(acc.id).padStart(4, '0')}`}
                  </div>

                  <div className="bank-card-footer">
                    <div>
                      <div className="bank-card-balance-label">Saldo Atual</div>
                      <div className="bank-card-balance-val">
                        {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(acc.saldoInicial || 0)}
                      </div>
                    </div>

                    {acc.chequeEspecial > 0 && (
                      <div className="bank-card-overdraft" title="Limite Cheque Especial">
                        Lmt: R$ {acc.chequeEspecial}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Account Specific Transaction History */}
      <div className="nimbus-card" style={{ padding: 0 }}>
        <div style={{ padding: '1.25rem 1.5rem', borderBottom: '1px solid var(--nimbus-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ fontSize: '1.1rem' }}>
            {selectedAccountObj ? `Histórico de Lançamentos — ${selectedAccountObj.nome}` : 'Lançamentos de Todas as Contas'}
          </h2>
          <span style={{ fontSize: '0.85rem', color: 'var(--nimbus-text-muted)' }}>
            {filteredTransactions.length} registro(s)
          </span>
        </div>
        <TransactionTable
          transactions={filteredTransactions}
          loading={loadingTransactions}
        />
      </div>

      {/* Account Modal */}
      <AccountModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={refetchAccounts}
      />
    </div>
  );
};

export default AccountsPage;
