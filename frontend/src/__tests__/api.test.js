import { describe, it, expect, beforeEach, vi } from 'vitest';
import api from '../services/api';

describe('API Axios Interceptor Unit Tests', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('should attach Bearer token to request headers if present in localStorage', async () => {
    localStorage.setItem('token', 'test-jwt-token');

    // Simulate request interceptor handler
    const requestHandler = api.interceptors.request.handlers[0].fulfilled;
    const config = { headers: {} };
    const resultConfig = requestHandler(config);

    expect(resultConfig.headers.Authorization).toBe('Bearer test-jwt-token');
  });

  it('should not attach Authorization header if no token exists in localStorage', () => {
    const requestHandler = api.interceptors.request.handlers[0].fulfilled;
    const config = { headers: {} };
    const resultConfig = requestHandler(config);

    expect(resultConfig.headers.Authorization).toBeUndefined();
  });

  it('should handle 401 unauthorized response by clearing storage and firing custom event', async () => {
    localStorage.setItem('token', 'expired-token');
    localStorage.setItem('user', JSON.stringify({ name: 'User' }));

    const dispatchEventSpy = vi.spyOn(window, 'dispatchEvent');

    const responseErrorHandler = api.interceptors.response.handlers[0].rejected;
    const mockError = {
      response: {
        status: 401,
        data: { message: 'Token expirado' },
      },
    };

    try {
      await responseErrorHandler(mockError);
    } catch (err) {
      expect(err.status).toBe(401);
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
      expect(dispatchEventSpy).toHaveBeenCalled();
    }
  });

  it('should format 400 validation error maps correctly', async () => {
    const responseErrorHandler = api.interceptors.response.handlers[0].rejected;
    const mockError = {
      response: {
        status: 400,
        data: {
          message: 'Erro de validação',
          errors: {
            email: 'E-mail inválido',
          },
        },
      },
    };

    try {
      await responseErrorHandler(mockError);
    } catch (err) {
      expect(err.status).toBe(400);
      expect(err.isValidationError).toBe(true);
      expect(err.fieldErrors.email).toBe('E-mail inválido');
    }
  });
});
