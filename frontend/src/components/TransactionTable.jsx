import React from 'react';
import Badge from './Badge';

/**
 * TransactionTable Component
 * @param {{
 *   transactions: Array<{
 *     id: number,
 *     descricao: string,
 *     valor: number,
 *     data: string,
 *     tipo: string,
 *     contaId: number,
 *     contaNome: string,
 *     categoriaId: number,
 *     categoriaNome: string
 *   }>,
 *   loading?: boolean,
 *   onEdit?: (transaction: object) => void,
 *   onDelete?: (id: number) => void
 * }} props
 */
export const TransactionTable = ({
  transactions = [],
  loading = false,
  onEdit,
  onDelete,
}) => {
  const formatCurrency = (val, tipo) => {
    const formatted = new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(val || 0);

    return tipo === 'RECEITA' ? `+ ${formatted}` : `- ${formatted}`;
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    // Format YYYY-MM-DD to DD/MM/YYYY
    const dateOnly = dateStr.split('T')[0];
    const parts = dateOnly.split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
  };

  if (loading) {
    return (
      <div className="table-container" style={{ padding: '2rem', textAlign: 'center' }}>
        <p style={{ color: 'var(--nimbus-text-muted)' }}>Carregando lançamentos...</p>
      </div>
    );
  }

  if (!transactions || transactions.length === 0) {
    return (
      <div className="table-container" style={{ padding: '3rem 1.5rem', textAlign: 'center' }}>
        <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>🔍</div>
        <h3 style={{ fontSize: '1.1rem', marginBottom: '0.25rem' }}>Nenhum lançamento encontrado</h3>
        <p style={{ color: 'var(--nimbus-text-muted)', fontSize: '0.875rem' }}>
          Não existem dados para exibir com os filtros atuais.
        </p>
      </div>
    );
  }

  return (
    <div className="table-container">
      <table className="nimbus-table">
        <thead>
          <tr>
            <th>Data</th>
            <th>Descrição</th>
            <th className="col-categoria">Categoria</th>
            <th>Conta</th>
            <th className="col-tipo">Tipo</th>
            <th style={{ textAlign: 'right' }}>Valor</th>
            {(onEdit || onDelete) && <th style={{ textAlign: 'center' }}>Ações</th>}
          </tr>
        </thead>
        <tbody>
          {transactions.map((tx) => (
            <tr key={tx.id}>
              <td style={{ whiteSpace: 'nowrap' }}>{formatDate(tx.data)}</td>
              <td style={{ fontWeight: 500 }}>{tx.descricao}</td>
              <td className="col-categoria">
                <span className="nimbus-badge badge-neutral">{tx.categoriaNome || tx.categoriaId}</span>
              </td>
              <td>{tx.contaNome || tx.contaId}</td>
              <td className="col-tipo">
                {tx.tipo === 'RECEITA' ? (
                  <Badge variant="success">RECEITA</Badge>
                ) : (
                  <Badge variant="danger">DESPESA</Badge>
                )}
              </td>
              <td
                className="font-mono"
                style={{
                  textAlign: 'right',
                  fontWeight: 600,
                  color: tx.tipo === 'RECEITA' ? 'var(--nimbus-success-text)' : 'var(--nimbus-danger-text)',
                }}
              >
                {formatCurrency(tx.valor, tx.tipo)}
              </td>
              {(onEdit || onDelete) && (
                <td style={{ textAlign: 'center', whiteSpace: 'nowrap' }}>
                  {onEdit && (
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={() => onEdit(tx)}
                      title="Editar"
                    >
                      ✏️
                    </button>
                  )}
                  {onDelete && (
                    <button
                      type="button"
                      className="btn btn-ghost btn-sm"
                      onClick={() => onDelete(tx.id)}
                      title="Excluir"
                      style={{ color: 'var(--nimbus-danger)' }}
                    >
                      🗑️
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default TransactionTable;
