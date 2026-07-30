import React from 'react';

/**
 * StatCard Component for summary metrics
 * @param {{
 *   title: string,
 *   value: number,
 *   icon?: React.ReactNode,
 *   variant?: 'primary'|'success'|'danger'|'info',
 *   subtitle?: string,
 *   loading?: boolean
 * }} props
 */
export const StatCard = ({
  title,
  value = 0,
  icon,
  variant = 'primary',
  subtitle,
  loading = false,
}) => {
  const formatCurrency = (val) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(val || 0);
  };

  return (
    <div className="nimbus-card stat-card">
      <div className="stat-header">
        <span className="stat-title">{title}</span>
        {icon && <div className={`stat-icon-wrapper ${variant}`}>{icon}</div>}
      </div>

      {loading ? (
        <div style={{ height: '32px', backgroundColor: 'var(--nimbus-border)', borderRadius: '4px', opacity: 0.6 }} />
      ) : (
        <div className="stat-value">{formatCurrency(value)}</div>
      )}

      {subtitle && <div style={{ fontSize: '0.8rem', color: 'var(--nimbus-text-muted)' }}>{subtitle}</div>}
    </div>
  );
};

export default StatCard;
