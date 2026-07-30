import { useState, useEffect, useCallback } from 'react';
import categoryService from '../services/categoryService';

export const useCategories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchCategories = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await categoryService.getAll();
      setCategories(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  const createCategory = async (categoryData) => {
    const created = await categoryService.create(categoryData);
    await fetchCategories();
    return created;
  };

  const updateCategory = async (id, categoryData) => {
    const updated = await categoryService.update(id, categoryData);
    await fetchCategories();
    return updated;
  };

  const deleteCategory = async (id) => {
    await categoryService.delete(id);
    await fetchCategories();
  };

  return {
    categories,
    loading,
    error,
    refetch: fetchCategories,
    createCategory,
    updateCategory,
    deleteCategory,
  };
};

export default useCategories;
