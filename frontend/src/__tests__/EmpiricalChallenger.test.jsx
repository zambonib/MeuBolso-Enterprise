import React from 'react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, act, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider, useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import api from '../services/api';
import TransactionModal from '../components/TransactionModal';
import StatCard from '../components/StatCard';
import DashboardPage from '../pages/DashboardPage';
import * as AuthContextModule from '../context/AuthContext';
import * as useAccountsModule from '../hooks/useAccounts';
import * as useCategoriesModule from '../hooks/useCategories';
import * as useTransactionsModule from '../hooks/useTransactions';

// Helper component for AuthContext tests
const AuthTestConsumer = () => {
  const { user, token, isAuthenticated, loading, authError, login, logout, checkAuth } = useAuth();
  return (
    <div>
      <div data-testid="loading">{loading ? 'LOADING' : 'READY'}</div>
      <div data-testid="auth-status">{isAuthenticated ? 'LOGGED_IN' : 'LOGGED_OUT'}</div>
      <div data-testid="user-email">{user?.email || 'NO_USER'}</div>
      <div data-testid="token">{token || 'NO_TOKEN'}</div>
      <div data-testid="auth-error">{authError || 'NO_ERROR'}</div>
      <button data-testid="btn-login" onClick={() => login({ email: 'test@meubolso.com', password: 'pass' })}>Login</button>
      <button data-testid="btn-logout" onClick={logout}>Logout</button>
      <button data-testid="btn-check" onClick={checkAuth}>CheckAuth</button>
    </div>
  );
};

describe('Empirical Verification: AuthContext & Token Expiration', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('1.1 Unauthenticated startup when localStorage is empty', async () => {
    vi.spyOn(authService, 'getToken').mockReturnValue(null);
    vi.spyOn(authService, 'getStoredUser').mockReturnValue(null);

    render(
      <AuthProvider>
        <AuthTestConsumer />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');
    expect(screen.getByTestId('user-email')).toHaveTextContent('NO_USER');
    expect(screen.getByTestId('token')).toHaveTextContent('NO_TOKEN');
  });

  it('1.2 Token expiration during checkAuth triggers logout and clears state', async () => {
    vi.spyOn(authService, 'getToken').mockReturnValue('expired-jwt-token');
    vi.spyOn(authService, 'getStoredUser').mockReturnValue({ id: 1, email: 'expired@test.com' });
    vi.spyOn(authService, 'getMe').mockRejectedValue({ status: 401, message: 'Token expirado' });
    const logoutSpy = vi.spyOn(authService, 'logout');

    await act(async () => {
      render(
        <AuthProvider>
          <AuthTestConsumer />
        </AuthProvider>
      );
    });

    expect(logoutSpy).toHaveBeenCalled();
    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');
    expect(screen.getByTestId('user-email')).toHaveTextContent('NO_USER');
  });

  it('1.3 Reacts to auth:unauthorized window event dispatched by Axios interceptor', async () => {
    const mockUser = { id: 1, email: 'active@test.com' };
    vi.spyOn(authService, 'getToken').mockReturnValue('valid-token');
    vi.spyOn(authService, 'getStoredUser').mockReturnValue(mockUser);
    vi.spyOn(authService, 'getMe').mockResolvedValue(mockUser);

    await act(async () => {
      render(
        <AuthProvider>
          <AuthTestConsumer />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');

    // Simulate Axios 401 custom event dispatch
    await act(async () => {
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');
    expect(screen.getByTestId('user-email')).toHaveTextContent('NO_USER');
  });

  it('1.4 Safely handles corrupted JSON in localStorage user key', () => {
    localStorage.setItem('user', '{ invalid_json: ');
    const user = authService.getStoredUser();
    expect(user).toBeNull();
  });
});

describe('Empirical Verification: Axios Interceptors & Error Formatting', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('2.1 Request interceptor attaches Authorization header when token exists', () => {
    localStorage.setItem('token', 'valid-jwt-xyz');
    const requestHandler = api.interceptors.request.handlers[0].fulfilled;
    const config = { headers: {} };
    const resultConfig = requestHandler(config);

    expect(resultConfig.headers.Authorization).toBe('Bearer valid-jwt-xyz');
  });

  it('2.2 Request interceptor does NOT attach Authorization header when token is missing', () => {
    const requestHandler = api.interceptors.request.handlers[0].fulfilled;
    const config = { headers: {} };
    const resultConfig = requestHandler(config);

    expect(resultConfig.headers.Authorization).toBeUndefined();
  });

  it('2.3 Response interceptor handles 401 by clearing storage and dispatching event', async () => {
    localStorage.setItem('token', 'exp-token');
    localStorage.setItem('user', JSON.stringify({ name: 'User' }));

    const eventSpy = vi.spyOn(window, 'dispatchEvent');
    const responseErrorHandler = api.interceptors.response.handlers[0].rejected;

    const mock401Error = {
      response: {
        status: 401,
        data: { message: 'Token expirado' },
      },
    };

    await expect(responseErrorHandler(mock401Error)).rejects.toEqual({
      status: 401,
      message: 'Sessão expirada ou não autorizada. Faça login novamente.',
    });

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(eventSpy).toHaveBeenCalledWith(expect.any(CustomEvent));
  });

  it('2.4 Response interceptor formats 400 validation error with fieldErrors map', async () => {
    const responseErrorHandler = api.interceptors.response.handlers[0].rejected;
    const mock400Error = {
      response: {
        status: 400,
        data: {
          message: 'Erro de validação',
          errors: {
            valor: 'O valor deve ser maior que zero.',
            descricao: 'Descrição obrigatória.',
          },
        },
      },
    };

    await expect(responseErrorHandler(mock400Error)).rejects.toEqual({
      status: 400,
      message: 'Erro de validação',
      fieldErrors: {
        valor: 'O valor deve ser maior que zero.',
        descricao: 'Descrição obrigatória.',
      },
      isValidationError: true,
    });
  });

  it('2.5 Response interceptor handles network disconnection (!error.response)', async () => {
    const responseErrorHandler = api.interceptors.response.handlers[0].rejected;
    const mockNetworkError = {};

    await expect(responseErrorHandler(mockNetworkError)).rejects.toEqual({
      message: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
      isNetworkError: true,
    });
  });
});

describe('Empirical Verification: Dynamic Category Filtering', () => {
  const sampleCategories = [
    { id: 1, nome: 'Salário', tipo: 'RECEITA' },
    { id: 2, nome: 'Investimentos', tipo: 'RECEITA' },
    { id: 3, nome: 'Alimentação', tipo: 'DESPESA' },
    { id: 4, nome: 'Moradia', tipo: 'DESPESA' },
  ];

  const sampleAccounts = [
    { id: 10, nome: 'Conta Corrente', tipo: 'CORRENTE' },
  ];

  it('3.1 Filters categories strictly by selected tipo (DESPESA by default)', () => {
    render(
      <TransactionModal
        isOpen={true}
        onClose={() => {}}
        onSubmit={async () => {}}
        accounts={sampleAccounts}
        categories={sampleCategories}
      />
    );

    // Initial tipo is DESPESA
    expect(screen.getByText('Categoria (DESPESA)')).toBeInTheDocument();
    expect(screen.getByText('Alimentação')).toBeInTheDocument();
    expect(screen.getByText('Moradia')).toBeInTheDocument();
    expect(screen.queryByText('Salário')).not.toBeInTheDocument();
    expect(screen.queryByText('Investimentos')).not.toBeInTheDocument();
  });

  it('3.2 Dynamically filters categories when tipo changes to RECEITA and resets category selection', () => {
    render(
      <TransactionModal
        isOpen={true}
        onClose={() => {}}
        onSubmit={async () => {}}
        accounts={sampleAccounts}
        categories={sampleCategories}
      />
    );

    // Click RECEITA button
    fireEvent.click(screen.getByText('+ RECEITA'));

    expect(screen.getByText('Categoria (RECEITA)')).toBeInTheDocument();
    expect(screen.getByText('Salário')).toBeInTheDocument();
    expect(screen.getByText('Investimentos')).toBeInTheDocument();
    expect(screen.queryByText('Alimentação')).not.toBeInTheDocument();
  });

  it('3.3 Edge case: empty category list handles empty dropdown gracefully', () => {
    render(
      <TransactionModal
        isOpen={true}
        onClose={() => {}}
        onSubmit={async () => {}}
        accounts={sampleAccounts}
        categories={[]}
      />
    );

    expect(screen.getByText('Selecione a categoria...')).toBeInTheDocument();
  });

  it('3.4 Edge case: case-sensitivity verification (e.g. lowercase c.tipo vs uppercase formData.tipo)', () => {
    const mixedCaseCategories = [
      { id: 1, nome: 'Freelance', tipo: 'receita' }, // lowercase
      { id: 2, nome: 'Vendas', tipo: 'RECEITA' },    // uppercase
    ];

    render(
      <TransactionModal
        isOpen={true}
        onClose={() => {}}
        onSubmit={async () => {}}
        accounts={sampleAccounts}
        categories={mixedCaseCategories}
      />
    );

    fireEvent.click(screen.getByText('+ RECEITA'));

    // Strict c.tipo === formData.tipo means 'receita' is excluded!
    expect(screen.getByText('Vendas')).toBeInTheDocument();
    expect(screen.queryByText('Freelance')).not.toBeInTheDocument();
  });
});

describe('Empirical Verification: Balance Aggregation Logic & Edge Cases', () => {
  it('4.1 Aggregates correctly with standard values', () => {
    const accounts = [
      { id: 1, nome: 'Banco A', saldoInicial: 1000 },
      { id: 2, nome: 'Banco B', saldoInicial: 2000 },
    ];
    const transactions = [
      { id: 1, tipo: 'RECEITA', valor: 500 },
      { id: 2, tipo: 'RECEITA', valor: 300 },
      { id: 3, tipo: 'DESPESA', valor: 200 },
    ];

    const totalSaldoInicial = accounts.reduce((acc, a) => acc + (a.saldoInicial || 0), 0);
    const totalReceitas = transactions.filter((t) => t.tipo === 'RECEITA').reduce((acc, t) => acc + (t.valor || 0), 0);
    const totalDespesas = transactions.filter((t) => t.tipo === 'DESPESA').reduce((acc, t) => acc + (t.valor || 0), 0);
    const balancoMensal = totalReceitas - totalDespesas;
    const saldoTotalCalculado = totalSaldoInicial + totalReceitas - totalDespesas;

    expect(totalSaldoInicial).toBe(3000);
    expect(totalReceitas).toBe(800);
    expect(totalDespesas).toBe(200);
    expect(balancoMensal).toBe(600);
    expect(saldoTotalCalculado).toBe(3600);
  });

  it('4.2 Edge Case: Empty account and transaction lists', () => {
    const accounts = [];
    const transactions = [];

    const totalSaldoInicial = accounts.reduce((acc, a) => acc + (a.saldoInicial || 0), 0);
    const totalReceitas = transactions.filter((t) => t.tipo === 'RECEITA').reduce((acc, t) => acc + (t.valor || 0), 0);
    const totalDespesas = transactions.filter((t) => t.tipo === 'DESPESA').reduce((acc, t) => acc + (t.valor || 0), 0);
    const balancoMensal = totalReceitas - totalDespesas;
    const saldoTotalCalculado = totalSaldoInicial + totalReceitas - totalDespesas;

    expect(totalSaldoInicial).toBe(0);
    expect(totalReceitas).toBe(0);
    expect(totalDespesas).toBe(0);
    expect(balancoMensal).toBe(0);
    expect(saldoTotalCalculado).toBe(0);
  });

  it('4.3 Edge Case: Negative initial balances (debts/credit cards)', () => {
    const accounts = [
      { id: 1, nome: 'Cartão de Crédito', saldoInicial: -1500.50 },
      { id: 2, nome: 'Conta Corrente', saldoInicial: 500.00 },
    ];
    const transactions = [
      { id: 1, tipo: 'RECEITA', valor: 1000 },
      { id: 2, tipo: 'DESPESA', valor: 300 },
    ];

    const totalSaldoInicial = accounts.reduce((acc, a) => acc + (a.saldoInicial || 0), 0);
    const totalReceitas = transactions.filter((t) => t.tipo === 'RECEITA').reduce((acc, t) => acc + (t.valor || 0), 0);
    const totalDespesas = transactions.filter((t) => t.tipo === 'DESPESA').reduce((acc, t) => acc + (t.valor || 0), 0);
    const balancoMensal = totalReceitas - totalDespesas;
    const saldoTotalCalculado = totalSaldoInicial + totalReceitas - totalDespesas;

    expect(totalSaldoInicial).toBe(-1000.50);
    expect(balancoMensal).toBe(700);
    expect(saldoTotalCalculado).toBe(-300.50);
  });

  it('4.4 Edge Case: Negative expense values cause double-negative math flaw', () => {
    const accounts = [{ id: 1, saldoInicial: 1000 }];
    // If a DESPESA transaction has a negative number stored in valor (-200)
    const transactions = [
      { id: 1, tipo: 'DESPESA', valor: -200 },
    ];

    const totalDespesas = transactions.filter((t) => t.tipo === 'DESPESA').reduce((acc, t) => acc + (t.valor || 0), 0);
    const balancoMensal = 0 - totalDespesas;
    const saldoTotalCalculado = 1000 + 0 - totalDespesas;

    // Empirical observation: totalDespesas = -200, balancoMensal = +200, saldoTotalCalculado = 1200!
    expect(totalDespesas).toBe(-200);
    expect(balancoMensal).toBe(200);
    expect(saldoTotalCalculado).toBe(1200);
  });

  it('4.5 Edge Case: String numbers from API payload trigger string concatenation bug', () => {
    const accounts = [{ id: 1, saldoInicial: "1000" }]; // String instead of Number
    const transactions = [{ id: 1, tipo: 'RECEITA', valor: "500" }];

    const totalSaldoInicial = accounts.reduce((acc, a) => acc + (a.saldoInicial || 0), 0);
    const totalReceitas = transactions.filter((t) => t.tipo === 'RECEITA').reduce((acc, t) => acc + (t.valor || 0), 0);

    // Initial accumulator 0 + "1000" produces string "01000"!
    expect(totalSaldoInicial).toBe("01000");
    expect(totalReceitas).toBe("0500");
  });

  it('4.6 StatCard formatting of 0, negative values, and missing values', () => {
    const { rerender } = render(<StatCard title="Saldo" value={0} />);
    expect(screen.getByText('R$ 0,00')).toBeInTheDocument();

    rerender(<StatCard title="Saldo" value={-150.75} />);
    expect(screen.getByText(/-R\$\s*150,75|R\$\s*-150,75/)).toBeInTheDocument();

    rerender(<StatCard title="Saldo" value={undefined} />);
    expect(screen.getByText('R$ 0,00')).toBeInTheDocument();
  });
});
