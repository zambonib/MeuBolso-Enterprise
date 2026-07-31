import React from 'react';

export const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="global-footer">
      <div>
        &copy; {currentYear} Todos os direitos reservados para a <strong>Dalzam</strong>.
      </div>
      <div>
        <span>Meu Bolso — Gestão Financeira</span>
      </div>
    </footer>
  );
};

export default Footer;
