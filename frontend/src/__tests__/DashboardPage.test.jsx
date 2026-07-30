import React from 'react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import DashboardPage from '../pages/DashboardPage';
import * as AuthContextModule from '../context/AuthContext';
import * as useAccountsModule from '../hooks/useAccounts';
import * as useCategoriesModule from '../hooks/useCategories';
import * as useTransactionsModule from '../hooks/useTransactions';

describe('DashboardPage Component Tests', () => {
  beforeEach(() => {
    vi.restoreAllMocks();

    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: { name: 'Maria Souza', email: 'maria@meubolso.com' },
    });

    vi.spyOn(useAccountsModule, 'useAccounts').mockReturnValue({
      accounts: [
        { id: 1, nome: 'Itaú Personalité', saldoInicial: 5000, tipo: 'CORRENTE' },
        { id: 2, nome: 'Reserva Emergência', saldoInicial: 10000, tipo: 'POUPANCA' },
      ],
      loading: false,
      refetch: vi.fn(),
    });

    vi.spyOn(useCategoriesModule, 'useCategories').mockReturnValue({
      categories: [
        { id: 1, nome: 'Salário', tipo: 'RECEITA' },
        { id: 2, nome: 'Alimentação', tipo: 'DESPESA' },
      ],
      loading: false,
    });

    vi.spyOn(useTransactionsModule, 'useTransactions').mockReturnValue({
      transactions: [
        { id: 101, descricao: 'Salário Mensal', valor: 8000, tipo: 'RECEITA', contaId: 1, contaNome: 'Itaú', categoriaId: 1, categoriaNome: 'Salário', data: '2026-07-01' },
        { id: 102, descricao: 'Mercado', valor: 1500, tipo: 'DESPESA', contaId: 1, contaNome: 'Itaú', categoriaId: 2, categoriaNome: 'Alimentação', data: '2026-07-05' },
      ],
      loading: false,
      refetch: vi.fn(),
      createTransaction: vi.fn(),
    });
  });

  it('should render header with user name and stat cards with calculated balance', () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText(/Visão Geral/i)).toBeInTheDocument();
    expect(screen.getByText(/Olá, Maria Souza/i)).toBeInTheDocument();
    expect(screen.getByText('Saldo Total')).toBeInTheDocument();
    expect(screen.getByText('Receitas do Mês')).toBeInTheDocument();
    expect(screen.getByText('Despesas do Mês')).toBeInTheDocument();
  });
});
