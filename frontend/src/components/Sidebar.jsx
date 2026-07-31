import React from 'react';
import { NavLink } from 'react-router-dom';

export const Sidebar = () => {
  return (
    <aside className="sidebar">
      <nav className="sidebar-nav">
        <div className="sidebar-section-title">Principal</div>
        <NavLink
          to="/dashboard"
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span>📊</span>
          <span>Dashboard</span>
        </NavLink>
        <NavLink
          to="/contas"
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span>💳</span>
          <span>Contas Bancárias</span>
        </NavLink>
        <NavLink
          to="/transacoes"
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span>💸</span>
          <span>Lançamentos</span>
        </NavLink>
      </nav>
    </aside>
  );
};

export default Sidebar;
