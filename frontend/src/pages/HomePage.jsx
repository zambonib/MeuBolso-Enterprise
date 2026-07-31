import React from 'react';
import { Link } from 'react-router-dom';
import Footer from '../components/Footer';

export const HomePage = () => {
  return (
    <div className="home-wrapper">
      {/* Public Header */}
      <header className="home-header">
        <div className="navbar-brand">
          <div className="navbar-brand-icon" style={{ display: 'flex', alignItems: 'center' }}>
            <img src="/logo.svg" alt="Logo Meu Bolso" style={{ width: '36px', height: '36px', objectFit: 'contain' }} />
          </div>
          <span style={{ fontSize: '1.35rem', fontWeight: 700 }}>Meu Bolso</span>
        </div>

        <div className="home-header-actions">
          <Link to="/login" className="btn btn-ghost btn-sm">
            Entrar
          </Link>
          <Link to="/register" className="btn btn-primary btn-sm">
            Criar Conta
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <section className="home-hero">
        <div className="home-hero-content">
          <span className="nimbus-badge badge-info" style={{ marginBottom: '1rem', padding: '0.4rem 0.8rem' }}>
            ✨ Gestão Financeira Inteligente
          </span>
          <h1 className="home-hero-title">
            O controle total das suas finanças na palma da sua mão.
          </h1>
          <p className="home-hero-subtitle">
            O <strong>Meu Bolso</strong> ajuda você e sua empresa a organizar receitas, despesas, cartões e múltiplas contas bancárias em uma plataforma simples, rápida e segura.
          </p>

          <div className="home-hero-cta">
            <Link to="/register" className="btn btn-primary" style={{ padding: '0.8rem 1.75rem', fontSize: '1rem' }}>
              Começar Agora Grátis 🚀
            </Link>
            <Link to="/login" className="btn btn-secondary" style={{ padding: '0.8rem 1.75rem', fontSize: '1rem' }}>
              Já tenho conta
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="home-features">
        <h2 style={{ textAlign: 'center', marginBottom: '2.5rem', fontSize: '1.8rem' }}>
          Por que usar o Meu Bolso?
        </h2>

        <div className="home-features-grid">
          <div className="nimbus-card feature-card">
            <div className="feature-icon">🏦</div>
            <h3>Multi-Contas</h3>
            <p>Cadastre suas contas correntes, poupanças e cartões de crédito em um só lugar para saber exatamente onde seu dinheiro está.</p>
          </div>

          <div className="nimbus-card feature-card">
            <div className="feature-icon">📈</div>
            <h3>Gestão de Lançamentos</h3>
            <p>Registre receitas e despesas com categorização simples e acompanhe o balanço financeiro em tempo real.</p>
          </div>

          <div className="nimbus-card feature-card">
            <div className="feature-icon">🔒</div>
            <h3>Segurança & Isolamento</h3>
            <p>Seus dados são 100% protegidos com tecnologia SaaS Multi-Tenant e criptografia de ponta a ponta.</p>
          </div>
        </div>
      </section>

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default HomePage;
