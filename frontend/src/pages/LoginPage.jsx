import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [authAlert, setAuthAlert] = useState(null);

  const validate = () => {
    const errs = {};
    if (!email.trim()) {
      errs.email = 'Informe o e-mail.';
    } else if (!/^\S+@\S+\.\S+$/.test(email)) {
      errs.email = 'Informe um e-mail válido.';
    }

    if (!password) {
      errs.password = 'Informe sua senha.';
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setAuthAlert(null);

    if (!validate()) return;

    setSubmitting(true);
    try {
      await login({ email: email.trim(), password });
      navigate('/dashboard');
    } catch (err) {
      setAuthAlert(err.message || 'E-mail ou senha incorretos.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-header">
          <img src="/logo.svg" alt="Logo Meu Bolso Enterprise" style={{ width: '48px', height: '48px', marginBottom: '0.5rem', objectFit: 'contain' }} />
          <h1 className="auth-title">Meu Bolso Enterprise</h1>
          <p className="auth-subtitle">Entrar no Sistema Financeiro Multi-tenant</p>
        </div>

        {authAlert && <div className="alert-danger">{authAlert}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">E-mail</label>
            <input
              type="email"
              className={`form-input ${errors.email ? 'is-invalid' : ''}`}
              placeholder="seu.email@empresa.com"
              value={email}
              autoFocus
              onChange={(e) => setEmail(e.target.value)}
            />
            {errors.email && <span className="form-error">{errors.email}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Senha</label>
            <div style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                className={`form-input ${errors.password ? 'is-invalid' : ''}`}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                style={{ position: 'absolute', right: '0.5rem', top: '50%', transform: 'translateY(-50%)' }}
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? 'Ocultar' : 'Exibir'}
              </button>
            </div>
            {errors.password && <span className="form-error">{errors.password}</span>}
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '1rem' }}
            disabled={submitting}
          >
            {submitting ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <div className="auth-footer">
          Não tem uma conta? <Link to="/register">Cadastre-se aqui</Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
