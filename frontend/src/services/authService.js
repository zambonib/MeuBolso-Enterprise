import api from './api';

export const authService = {
  /**
   * Register a new user
   * @param {{ username: string, email: string, password: string, name: string }} data
   */
  async register(data) {
    const response = await api.post('/api/auth/register', data);
    const { token, user } = response.data;
    if (token) {
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    }
    return response.data;
  },

  /**
   * Authenticate user with credentials
   * @param {{ email: string, password: string }} credentials
   */
  async login(credentials) {
    const response = await api.post('/api/auth/login', credentials);
    const { token, user } = response.data;
    if (token) {
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    }
    return response.data;
  },

  /**
   * Fetch current logged-in user details
   */
  async getMe() {
    const response = await api.get('/api/auth/me');
    const user = response.data;
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    }
    return user;
  },

  /**
   * Log out user by clearing stored token and cached user
   */
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  /**
   * Retrieve active token from localStorage
   */
  getToken() {
    return localStorage.getItem('token');
  },

  /**
   * Retrieve cached user object from localStorage
   */
  getStoredUser() {
    const userStr = localStorage.getItem('user');
    try {
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  },
};

export default authService;
