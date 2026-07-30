import { useState, useEffect, useCallback } from 'react';
import transactionService from '../services/transactionService';

export const useTransactions = (initialFilters = {}) => {
  const [transactions, setTransactions] = useState([]);
  const [filters, setFilters] = useState(initialFilters);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTransactions = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await transactionService.getAll(filters);
      setTransactions(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchTransactions();
  }, [fetchTransactions]);

  const createTransaction = async (data) => {
    const created = await transactionService.create(data);
    await fetchTransactions();
    return created;
  };

  const updateTransaction = async (id, data) => {
    const updated = await transactionService.update(id, data);
    await fetchTransactions();
    return updated;
  };

  const deleteTransaction = async (id) => {
    await transactionService.delete(id);
    await fetchTransactions();
  };

  return {
    transactions,
    filters,
    setFilters,
    loading,
    error,
    refetch: fetchTransactions,
    createTransaction,
    updateTransaction,
    deleteTransaction,
  };
};

export default useTransactions;
