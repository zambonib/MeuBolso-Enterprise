import React from 'react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { AuthProvider, useAuth } from '../context/AuthContext';
import authService from '../services/authService';

const TestComponent = () => {
  const { user, isAuthenticated, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="auth-status">{isAuthenticated ? 'LOGGED_IN' : 'LOGGED_OUT'}</span>
      <span data-testid="user-name">{user?.name || 'GUEST'}</span>
      <button onClick={() => login({ email: 'test@meubolso.com', password: '123' })}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

describe('AuthContext Integration Tests', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('should initialize as logged out when no stored token exists', async () => {
    vi.spyOn(authService, 'getToken').mockReturnValue(null);
    vi.spyOn(authService, 'getStoredUser').mockReturnValue(null);

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_OUT');
    expect(screen.getByTestId('user-name')).toHaveTextContent('GUEST');
  });

  it('should restore authenticated session from localStorage', async () => {
    const mockUser = { id: 1, name: 'João Silva', email: 'joao@meubolso.com' };
    vi.spyOn(authService, 'getToken').mockReturnValue('valid-token');
    vi.spyOn(authService, 'getStoredUser').mockReturnValue(mockUser);
    vi.spyOn(authService, 'getMe').mockResolvedValue(mockUser);

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('auth-status')).toHaveTextContent('LOGGED_IN');
    expect(screen.getByTestId('user-name')).toHaveTextContent('João Silva');
  });
});
