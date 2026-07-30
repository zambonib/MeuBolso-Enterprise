import { useState, useEffect, useCallback } from 'react';
import accountService from '../services/accountService';

export const useAccounts = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAccounts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await accountService.getAll();
      setAccounts(data);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAccounts();
  }, [fetchAccounts]);

  const createAccount = async (accountData) => {
    const created = await accountService.create(accountData);
    await fetchAccounts();
    return created;
  };

  const updateAccount = async (id, accountData) => {
    const updated = await accountService.update(id, accountData);
    await fetchAccounts();
    return updated;
  };

  const deleteAccount = async (id) => {
    await accountService.delete(id);
    await fetchAccounts();
  };

  return {
    accounts,
    loading,
    error,
    refetch: fetchAccounts,
    createAccount,
    updateAccount,
    deleteAccount,
  };
};

export default useAccounts;
