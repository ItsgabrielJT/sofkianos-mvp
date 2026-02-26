import React from 'react';

/**
 * Mapa de colores CSS por categoría de kudo.
 * Cada categoría tiene un color de fondo y texto asociado.
 */
const CATEGORY_STYLES: Record<string, { bg: string; text: string }> = {
  Innovation: { bg: 'bg-blue-500/20', text: 'text-blue-400' },
  Teamwork: { bg: 'bg-green-500/20', text: 'text-green-400' },
  Passion: { bg: 'bg-red-500/20', text: 'text-red-400' },
  Mastery: { bg: 'bg-yellow-500/20', text: 'text-yellow-400' },
};

/** Estilo fallback para categorías desconocidas. */
const DEFAULT_STYLE = { bg: 'bg-zinc-500/20', text: 'text-zinc-400' };

/**
 * Propiedades del componente CategoryBadge.
 *
 * @interface CategoryBadgeProps
 * @property {string} category - Nombre de la categoría a mostrar.
 */
interface CategoryBadgeProps {
  category: string;
}

/**
 * Badge visual con color según la categoría del kudo.
 *
 * Renderiza un chip/badge con colores diferenciados por categoría:
 * - Innovation → azul
 * - Teamwork → verde
 * - Passion → rojo
 * - Mastery → amarillo
 *
 * Si la categoría no está en el mapa, usa un estilo gris por defecto.
 *
 * @component
 * @param {CategoryBadgeProps} props - Propiedades del componente.
 * @returns {JSX.Element} Span con badge estilizado.
 *
 * @example
 * ```tsx
 * <CategoryBadge category="Teamwork" />
 * ```
 */
export const CategoryBadge: React.FC<CategoryBadgeProps> = ({ category }) => {
  const style = CATEGORY_STYLES[category] ?? DEFAULT_STYLE;

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${style.bg} ${style.text}`}
    >
      {category}
    </span>
  );
};
