import api from './api';

export const transactionService = {
  /**
   * Fetch transactions with optional filters
   * @param {{ contaId?: number|string, categoriaId?: number|string, tipo?: 'RECEITA'|'DESPESA' }} filters
   */
  async getAll(filters = {}) {
    const params = {};
    if (filters.contaId) params.contaId = filters.contaId;
    if (filters.categoriaId) params.categoriaId = filters.categoriaId;
    if (filters.tipo) params.tipo = filters.tipo;

    const response = await api.get('/api/transacoes', { params });
    return response.data;
  },

  /**
   * Fetch transaction by ID
   * @param {number} id
   */
  async getById(id) {
    const response = await api.get(`/api/transacoes/${id}`);
    return response.data;
  },

  /**
   * Create new transaction
   * @param {{ descricao: string, valor: number, data: string, tipo: string, contaId: number, categoriaId: number }} data
   */
  async create(data) {
    const response = await api.post('/api/transacoes', data);
    return response.data;
  },

  /**
   * Update existing transaction
   * @param {number} id
   * @param {{ descricao: string, valor: number, data: string, tipo: string, contaId: number, categoriaId: number }} data
   */
  async update(id, data) {
    const response = await api.put(`/api/transacoes/${id}`, data);
    return response.data;
  },

  /**
   * Delete transaction by ID
   * @param {number} id
   */
  async delete(id) {
    await api.delete(`/api/transacoes/${id}`);
  },
};

export default transactionService;
