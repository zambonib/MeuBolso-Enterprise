import api from './api';

export const categoryService = {
  /**
   * Fetch all categories for current user
   */
  async getAll() {
    const response = await api.get('/api/categorias');
    return response.data;
  },

  /**
   * Fetch category by ID
   * @param {number} id
   */
  async getById(id) {
    const response = await api.get(`/api/categorias/${id}`);
    return response.data;
  },

  /**
   * Create new category
   * @param {{ nome: string, tipo: string }} data
   */
  async create(data) {
    const response = await api.post('/api/categorias', data);
    return response.data;
  },

  /**
   * Update existing category
   * @param {number} id
   * @param {{ nome: string, tipo: string }} data
   */
  async update(id, data) {
    const response = await api.put(`/api/categorias/${id}`, data);
    return response.data;
  },

  /**
   * Delete category by ID
   * @param {number} id
   */
  async delete(id) {
    await api.delete(`/api/categorias/${id}`);
  },
};

export default categoryService;
