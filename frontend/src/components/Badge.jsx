import React from 'react';

/**
 * Badge component for displaying transaction types, account status, etc.
 * @param {{ children: React.ReactNode, variant?: 'success'|'danger'|'info'|'neutral', className?: string }} props
 */
export const Badge = ({ children, variant = 'neutral', className = '' }) => {
  const variantClass = `badge-${variant}`;
  return (
    <span className={`nimbus-badge ${variantClass} ${className}`}>
      {children}
    </span>
  );
};

export default Badge;
