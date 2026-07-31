import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [theme, setTheme] = useState(() => {
    return document.documentElement.getAttribute('data-theme') || 'light';
  });

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(nextTheme);
    document.documentElement.setAttribute('data-theme', nextTheme);
  };

  const getInitial = () => {
    if (!user) return 'U';
    const nameStr = user.name || user.username || user.email || 'U';
    return nameStr.charAt(0).toUpperCase();
  };

  return (
    <>
      <header className="navbar">
        <div className="navbar-brand">
          <div className="navbar-brand-icon" style={{ display: 'flex', alignItems: 'center' }}>
            <img src="/logo.svg" alt="Logo Meu Bolso" style={{ width: '32px', height: '32px', objectFit: 'contain' }} />
          </div>
          <span>Meu Bolso</span>
        </div>

        {/* Desktop Navbar Actions */}
        <div className="navbar-actions desktop-only">
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={toggleTheme}
            title="Alternar Tema"
          >
            {theme === 'light' ? '🌙 Escuro' : '☀️ Claro'}
          </button>

          {user && (
            <div className="user-pill">
              <div className="user-avatar">{getInitial()}</div>
              <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.2 }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>{user.name || user.username}</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--nimbus-text-muted)' }}>{user.email}</span>
              </div>
            </div>
          )}

          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={logout}
          >
            Sair
          </button>
        </div>

        {/* Mobile Hamburger Button */}
        <button
          type="button"
          className="mobile-hamburger-btn mobile-only"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          aria-label="Abrir Menu"
        >
          {isMobileMenuOpen ? '✖' : '☰'}
        </button>
      </header>

      {/* Mobile Menu Drawer Overlay */}
      {isMobileMenuOpen && (
        <div className="mobile-drawer-overlay" onClick={() => setIsMobileMenuOpen(false)}>
          <div className="mobile-drawer" onClick={(e) => e.stopPropagation()}>
            {user && (
              <div className="mobile-user-header">
                <div className="user-avatar" style={{ width: '36px', height: '36px', fontSize: '1.1rem' }}>{getInitial()}</div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>{user.name || user.username}</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--nimbus-text-muted)' }}>{user.email}</div>
                </div>
              </div>
            )}

            <div className="mobile-nav-links">
              <NavLink
                to="/dashboard"
                className={({ isActive }) => `mobile-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => setIsMobileMenuOpen(false)}
              >
                <span>📊 Dashboard</span>
              </NavLink>

              <NavLink
                to="/transacoes"
                className={({ isActive }) => `mobile-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => setIsMobileMenuOpen(false)}
              >
                <span>💸 Lançamentos</span>
              </NavLink>
            </div>

            <div className="mobile-drawer-footer">
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                style={{ width: '100%', justifyContent: 'flex-start' }}
                onClick={toggleTheme}
              >
                {theme === 'light' ? '🌙 Modo Escuro' : '☀️ Modo Claro'}
              </button>

              <button
                type="button"
                className="btn btn-danger btn-sm"
                style={{ width: '100%', marginTop: '0.5rem' }}
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  logout();
                }}
              >
                🚪 Sair
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Navbar;
