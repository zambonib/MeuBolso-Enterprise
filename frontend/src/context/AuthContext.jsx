import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import authService from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => authService.getStoredUser());
  const [token, setToken] = useState(() => authService.getToken());
  const [loading, setLoading] = useState(true);
  const [authError, setAuthError] = useState(null);

  const logout = useCallback(() => {
    authService.logout();
    setUser(null);
    setToken(null);
    setAuthError(null);
  }, []);

  const checkAuth = useCallback(async () => {
    const storedToken = authService.getToken();
    if (!storedToken) {
      setUser(null);
      setToken(null);
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      const currentUser = await authService.getMe();
      setUser(currentUser);
      setToken(storedToken);
      setAuthError(null);
    } catch (err) {
      console.warn('Auto-login session invalid or expired:', err);
      if (!err || !err.isNetworkError) {
        logout();
      }
    } finally {
      setLoading(false);
    }
  }, [logout]);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  useEffect(() => {
    const handleUnauthorized = () => {
      logout();
    };

    if (typeof window !== 'undefined') {
      window.addEventListener('auth:unauthorized', handleUnauthorized);
    }
    return () => {
      if (typeof window !== 'undefined') {
        window.removeEventListener('auth:unauthorized', handleUnauthorized);
      }
    };
  }, [logout]);

  const login = async (credentials) => {
    setAuthError(null);
    try {
      const response = await authService.login(credentials);
      setUser(response.user);
      setToken(response.token);
      return response;
    } catch (err) {
      setAuthError(err.message || 'Erro ao realizar login');
      throw err;
    }
  };

  const register = async (userData) => {
    setAuthError(null);
    try {
      const response = await authService.register(userData);
      setUser(response.user);
      setToken(response.token);
      return response;
    } catch (err) {
      setAuthError(err.message || 'Erro ao realizar cadastro');
      throw err;
    }
  };

  const value = {
    user,
    token,
    loading,
    authError,
    isAuthenticated: !!user && !!token,
    login,
    register,
    logout,
    checkAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser utilizado dentro de um AuthProvider');
  }
  return context;
};

export default AuthContext;
