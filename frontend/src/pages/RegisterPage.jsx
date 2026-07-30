import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const RegisterPage = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [formData, setFormData] = useState({
    name: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [authAlert, setAuthAlert] = useState(null);

  const validate = () => {
    const errs = {};
    if (!formData.name.trim() || formData.name.length < 2) {
      errs.name = 'O nome deve ter entre 2 e 100 caracteres.';
    }

    if (!formData.username.trim() || !/^[a-zA-Z0-9_]{3,50}$/.test(formData.username)) {
      errs.username = 'Usuário deve ter 3 a 50 caracteres (apenas letras, números e _).';
    }

    if (!formData.email.trim() || !/^\S+@\S+\.\S+$/.test(formData.email)) {
      errs.email = 'Informe um e-mail válido.';
    }

    if (!formData.password || formData.password.length < 6) {
      errs.password = 'A senha deve ter no mínimo 6 caracteres.';
    }

    if (formData.password !== formData.confirmPassword) {
      errs.confirmPassword = 'As senhas não coincidem.';
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
      await register({
        name: formData.name.trim(),
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password,
      });
      navigate('/dashboard');
    } catch (err) {
      if (err && err.fieldErrors) {
        setErrors(err.fieldErrors);
      }
      setAuthAlert(err.message || 'Erro ao realizar cadastro.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-header">
          <div style={{ fontSize: '2.5rem' }}>✨</div>
          <h1 className="auth-title">Criar Conta Enterprise</h1>
          <p className="auth-subtitle">Registre-se no Meu Bolso Enterprise</p>
        </div>

        {authAlert && <div className="alert-danger">{authAlert}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Nome Completo</label>
            <input
              type="text"
              className={`form-input ${errors.name ? 'is-invalid' : ''}`}
              placeholder="Maria Silva"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
            {errors.name && <span className="form-error">{errors.name}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Nome de Usuário</label>
            <input
              type="text"
              className={`form-input ${errors.username ? 'is-invalid' : ''}`}
              placeholder="mariasilva"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
            />
            {errors.username && <span className="form-error">{errors.username}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">E-mail</label>
            <input
              type="email"
              className={`form-input ${errors.email ? 'is-invalid' : ''}`}
              placeholder="maria@empresa.com"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
            {errors.email && <span className="form-error">{errors.email}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Senha</label>
            <input
              type="password"
              className={`form-input ${errors.password ? 'is-invalid' : ''}`}
              placeholder="••••••••"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            />
            {errors.password && <span className="form-error">{errors.password}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Confirmar Senha</label>
            <input
              type="password"
              className={`form-input ${errors.confirmPassword ? 'is-invalid' : ''}`}
              placeholder="••••••••"
              value={formData.confirmPassword}
              onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
            />
            {errors.confirmPassword && <span className="form-error">{errors.confirmPassword}</span>}
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '1rem' }}
            disabled={submitting}
          >
            {submitting ? 'Criando Conta...' : 'Criar Conta'}
          </button>
        </form>

        <div className="auth-footer">
          Já possui conta? <Link to="/login">Faça Login</Link>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
