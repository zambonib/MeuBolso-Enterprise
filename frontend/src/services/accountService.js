import api from './api';

export const accountService = {
  /**
   * Fetch all accounts for current user
   */
  async getAll() {
    const response = await api.get('/api/contas');
    return response.data;
  },

  /**
   * Fetch account by ID
   * @param {number} id
   */
  async getById(id) {
    const response = await api.get(`/api/contas/${id}`);
    return response.data;
  },

  /**
   * Create new account
   * @param {{ nome: string, saldoInicial: number, tipo: string }} data
   */
  async create(data) {
    const response = await api.post('/api/contas', data);
    return response.data;
  },

  /**
   * Update existing account
   * @param {number} id
   * @param {{ nome: string, saldoInicial: number, tipo: string }} data
   */
  async update(id, data) {
    const response = await api.put(`/api/contas/${id}`, data);
    return response.data;
  },

  /**
   * Delete account by ID
   * @param {number} id
   */
  async delete(id) {
    await api.delete(`/api/contas/${id}`);
  },
};

export default accountService;
