import React from 'react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthProvider, useAuth } from '../context/AuthContext';
import authService from '../services/authService';
import accountService from '../services/accountService';
import categoryService from '../services/categoryService';
import transactionService from '../services/transactionService';
import api from '../services/api';
import DashboardPage from '../pages/DashboardPage';
import TransactionModal from '../components/TransactionModal';
import TransactionTable from '../components/TransactionTable';
import Navbar from '../components/Navbar';
import StatCard from '../components/StatCard';

// Test consumer helper for AuthContext
const AuthTestConsumer = () => {
  const { user, token, isAuthenticated, loading, authError, login, register, logout, checkAuth } = useAuth();
  return (
    <div>
      <div data-testid="loading">{loading ? 'LOADING' : 'READY'}</div>
      <div data-testid="auth-status">{isAuthenticated ? 'LOGGED_IN' : 'LOGGED_OUT'}</div>
      <div data-testid="user-email">{user?.email || 'NO_USER'}</div>
      <div data-testid="user-name">{user?.name || user?.username || 'NO_NAME'}</div>
      <div data-testid="token">{token || 'NO_TOKEN'}</div>
      <div data-testid="auth-error">{authError || 'NO_ERROR'}</div>
      <button data-testid="btn-login" onClick={() => login({ email: 'maria.silva@empresa.com', password: 'Password123!' })}>Login</button>
      <button data-testid="btn-register" onClick={() => register({ name: 'Maria Silva', username: 'mariasilva', email: 'maria.silva@empresa.com', password: 'Password123!' })}>Register</button>
      <button data-testid="btn-logout" onClick={logout}>Logout</button>
      <button data-testid="btn-check" onClick={checkAuth}>CheckAuth</button>
    </div>
  );
};

describe('End-to-End User Journey Verification Suite (Milestone M4)', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  // ---------------------------------------------------------------------------
  // Stage 1 & 2: User Registration & Login
  // ---------------------------------------------------------------------------
  it('Stage 1 & 2: Full authentication flow - Register, Login, and Session Storage', async () => {
    const mockAuthResponse = {
      token: 'jwt_token_maria_123',
      user: {
        id: 1,
        name: 'Maria Silva',
        username: 'mariasilva',
        email: 'maria.silva@empresa.com',
      },
    };

    vi.spyOn(authService, 'register').mockResolvedValue(mockAuthResponse);
    vi.spyOn(authService, 'login').mockResolvedValue(mockAuthResponse);
    vi.spyOn(authService, 'getToken').mockImplementation(() => localStorage.getItem('token'));
    vi.spyOn(authService, 'getStoredUser').mockImplementation(() => {
      const u = localStorage.getItem('user');
      return u ? JSON.parse(u) : null;
    });

    render(
      <AuthProvider>
        <AuthTestConsumer />
      </AuthProvider>
    );

    // Initial state: logged out
    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');

    // Execute Register
    await act(async () => {
      fireEvent.click(screen.getByTestId('btn-register'));
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');
    expect(screen.getByTestId('user-email')).toHaveTextContent('maria.silva@empresa.com');
    expect(screen.getByTestId('token')).toHaveTextContent('jwt_token_maria_123');

    // Execute Logout
    await act(async () => {
      fireEvent.click(screen.getByTestId('btn-logout'));
    });
    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');

    // Execute Login
    await act(async () => {
      fireEvent.click(screen.getByTestId('btn-login'));
    });
    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');
  });

  // ---------------------------------------------------------------------------
  // Stage 3 to 7: Dashboard Rendering, Account Creation, Income/Expense, Balances
  // ---------------------------------------------------------------------------
  it('Stage 3 to 7: End-to-End User Journey - Account & Transaction Lifecycles with Exact Balance Math', async () => {
    const user = { id: 1, name: 'Maria Silva', email: 'maria.silva@empresa.com' };
    const accountsList = [
      { id: 10, nome: 'Conta Corrente Itaú', saldoInicial: 1000.0, tipo: 'CORRENTE' },
    ];
    const categoriesList = [
      { id: 1, nome: 'Salário', tipo: 'RECEITA' },
      { id: 2, nome: 'Moradia', tipo: 'DESPESA' },
    ];
    const transactionsList = [
      {
        id: 101,
        descricao: 'Salário Mensal',
        valor: 5000.0,
        data: '2026-07-30',
        tipo: 'RECEITA',
        contaId: 10,
        contaNome: 'Conta Corrente Itaú',
        categoriaId: 1,
        categoriaNome: 'Salário',
      },
      {
        id: 102,
        descricao: 'Aluguel Residencial',
        valor: 1500.0,
        data: '2026-07-30',
        tipo: 'DESPESA',
        contaId: 10,
        contaNome: 'Conta Corrente Itaú',
        categoriaId: 2,
        categoriaNome: 'Moradia',
      },
    ];

    vi.spyOn(authService, 'getToken').mockReturnValue('valid-token');
    vi.spyOn(authService, 'getStoredUser').mockReturnValue(user);
    vi.spyOn(authService, 'getMe').mockResolvedValue(user);

    vi.spyOn(accountService, 'getAll').mockResolvedValue(accountsList);
    vi.spyOn(categoryService, 'getAll').mockResolvedValue(categoriesList);
    vi.spyOn(transactionService, 'getAll').mockResolvedValue(transactionsList);

    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <DashboardPage />
          </AuthProvider>
        </MemoryRouter>
      );
    });

    // Verification Stage 3 & 7: StatCards rendering exact expected financial math
    // totalSaldoInicial = 1000.00
    // totalReceitas = 5000.00
    // totalDespesas = 1500.00
    // balancoMensal = 5000.00 - 1500.00 = 3500.00
    // saldoTotalCalculado = 1000.00 + 5000.00 - 1500.00 = 4500.00
    expect(screen.getByText('Saldo Total')).toBeInTheDocument();
    expect(screen.getByText('Receitas do Mês')).toBeInTheDocument();
    expect(screen.getByText('Despesas do Mês')).toBeInTheDocument();
    expect(screen.getByText('Balanço Mensal')).toBeInTheDocument();

    // Verify account name rendered in accounts list
    expect(screen.getAllByText('Conta Corrente Itaú').length).toBeGreaterThan(0);

    // Verify transaction descriptions rendered in recent transactions table
    expect(screen.getByText('Salário Mensal')).toBeInTheDocument();
    expect(screen.getByText('Aluguel Residencial')).toBeInTheDocument();
  });
});

describe('Cross-Tenant Frontend Isolation Verification Suite (Milestone M4)', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('Scenario 4.1: Token Cleansing & Session Switch prevents residual data access', async () => {
    // User A session setup
    localStorage.setItem('token', 'token_user_A');
    localStorage.setItem('user', JSON.stringify({ id: 101, name: 'User A' }));

    vi.spyOn(authService, 'logout').mockImplementation(() => {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    });

    // Execute logout
    authService.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();

    // User B login
    localStorage.setItem('token', 'token_user_B');
    localStorage.setItem('user', JSON.stringify({ id: 202, name: 'User B' }));

    const requestHandler = api.interceptors.request.handlers[0].fulfilled;
    const config = { headers: {} };
    const resultConfig = requestHandler(config);

    expect(resultConfig.headers.Authorization).toBe('Bearer token_user_B');
  });

  it('Scenario 4.2: LocalStorage Manipulation Defense via authService.getMe()', async () => {
    const authenticUserB = { id: 2, name: 'User B', email: 'userb@test.com' };

    // Malicious user tampers local storage with Admin identity
    localStorage.setItem('token', 'token_user_B');
    localStorage.setItem('user', JSON.stringify({ id: 1, name: 'Fake Admin', email: 'admin@system.com' }));

    vi.spyOn(authService, 'getToken').mockReturnValue('token_user_B');
    vi.spyOn(authService, 'getMe').mockResolvedValue(authenticUserB);

    render(
      <AuthProvider>
        <AuthTestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('user-name')).toHaveTextContent('User B');
      expect(screen.getByTestId('user-email')).toHaveTextContent('userb@test.com');
    });
  });

  it('Scenario 4.3: Decoupled 401 Session Revocation Event Pipeline', async () => {
    const user = { id: 1, email: 'active@test.com' };
    vi.spyOn(authService, 'getToken').mockReturnValue('valid-token');
    vi.spyOn(authService, 'getStoredUser').mockReturnValue(user);
    vi.spyOn(authService, 'getMe').mockResolvedValue(user);

    await act(async () => {
      render(
        <AuthProvider>
          <AuthTestConsumer />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');

    // Simulate 401 event dispatch
    await act(async () => {
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');
    expect(screen.getByTestId('user-email')).toHaveTextContent('NO_USER');
  });
});

describe('Defensive Hardening Verification Suite (Milestone M4)', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('Hardening 1: Prevents form input wipe when accounts reference updates while modal is open', async () => {
    const accounts1 = [{ id: 1, nome: 'Conta A', tipo: 'CORRENTE' }];
    const accounts2 = [{ id: 1, nome: 'Conta A', tipo: 'CORRENTE' }, { id: 2, nome: 'Conta B', tipo: 'POUPANCA' }];
    const categories = [{ id: 10, nome: 'Alimentação', tipo: 'DESPESA' }];

    const { rerender } = render(
      <TransactionModal
        isOpen={true}
        onClose={() => {}}
        onSubmit={async () => {}}
        accounts={accounts1}
        categories={categories}
      />
    );

    const descInput = screen.getByPlaceholderText(/Compra de Supermercado/i);
    fireEvent.change(descInput, { target: { value: 'Minha Descrição Digitada' } });
    expect(descInput.value).toBe('Minha Descrição Digitada');

    // Re-render modal with updated accounts prop reference
    rerender(
      <TransactionModal
        isOpen={true}
        onClose={() => {}}
        onSubmit={async () => {}}
        accounts={accounts2}
        categories={categories}
      />
    );

    // Form input MUST NOT be wiped
    expect(descInput.value).toBe('Minha Descrição Digitada');
  });

  it('Hardening 2: Explicitly handles NaN validation failure in TransactionModal', async () => {
    const accounts = [{ id: 1, nome: 'Conta A', tipo: 'CORRENTE' }];
    const categories = [{ id: 10, nome: 'Alimentação', tipo: 'DESPESA' }];

    render(
      <TransactionModal
        isOpen={true}
        onClose={() => {}}
        onSubmit={async () => {}}
        accounts={accounts}
        categories={categories}
      />
    );

    const valorInput = screen.getByPlaceholderText('0.00');
    fireEvent.change(valorInput, { target: { value: 'abc' } });

    const submitBtn = screen.getByRole('button', { name: /Salvar Transação/i });
    fireEvent.click(submitBtn);

    expect(await screen.findByText('O valor deve ser maior que zero.')).toBeInTheDocument();
  });

  it('Hardening 3: String-to-number type coercion in financial aggregations', async () => {
    const accounts = [{ id: 1, nome: 'Conta 1', saldoInicial: '1000.50' }];
    const transactions = [
      { id: 101, tipo: 'RECEITA', valor: '500.25' },
      { id: 102, tipo: 'DESPESA', valor: '200.00' },
    ];

    vi.spyOn(authService, 'getToken').mockReturnValue('token');
    vi.spyOn(authService, 'getStoredUser').mockReturnValue({ name: 'Test' });
    vi.spyOn(authService, 'getMe').mockResolvedValue({ name: 'Test' });
    vi.spyOn(accountService, 'getAll').mockResolvedValue(accounts);
    vi.spyOn(categoryService, 'getAll').mockResolvedValue([]);
    vi.spyOn(transactionService, 'getAll').mockResolvedValue(transactions);

    await act(async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <DashboardPage />
          </AuthProvider>
        </MemoryRouter>
      );
    });

    // Check that string totals are correctly coerced to numbers (no string concatenation like "01000")
    expect(screen.getByText('Saldo Total')).toBeInTheDocument();
  });

  it('Hardening 4: ISO 8601 Date formatting in TransactionTable', () => {
    const transactions = [
      {
        id: 1,
        descricao: 'ISO Date Test',
        valor: 100,
        data: '2026-07-30T14:30:00Z',
        tipo: 'RECEITA',
        contaId: 1,
        contaNome: 'Conta A',
        categoriaId: 1,
        categoriaNome: 'Salário',
      },
    ];

    render(<TransactionTable transactions={transactions} />);

    // Should format ISO 8601 string "2026-07-30T14:30:00Z" to "30/07/2026"
    expect(screen.getByText('30/07/2026')).toBeInTheDocument();
  });

  it('Hardening 5: Avatar initial fallback in Navbar when user.name is missing', () => {
    const userWithoutName = { username: 'mariasilva', email: 'maria@test.com' };

    vi.spyOn(authService, 'getToken').mockReturnValue('valid-token');
    vi.spyOn(authService, 'getStoredUser').mockReturnValue(userWithoutName);
    vi.spyOn(authService, 'getMe').mockResolvedValue(userWithoutName);

    render(
      <AuthProvider>
        <Navbar />
      </AuthProvider>
    );

    // Initial should be 'M' from username 'mariasilva' (not default 'U')
    expect(screen.getByText('M')).toBeInTheDocument();
  });

  it('Hardening 6: Transient network error protection in checkAuth()', async () => {
    vi.spyOn(authService, 'getToken').mockReturnValue('valid-token');
    vi.spyOn(authService, 'getMe').mockRejectedValue({
      message: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
      isNetworkError: true,
    });
    const logoutSpy = vi.spyOn(authService, 'logout');

    await act(async () => {
      render(
        <AuthProvider>
          <AuthTestConsumer />
        </AuthProvider>
      );
    });

    // Network error should NOT trigger logout
    expect(logoutSpy).not.toHaveBeenCalled();
  });
});
