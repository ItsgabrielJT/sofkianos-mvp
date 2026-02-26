import React from 'react';
import { ArrowDown, ArrowUp } from 'lucide-react';
import type { SortDirection } from '../../types/kudos.types';

/**
 * Propiedades del componente SortToggle.
 *
 * @interface SortToggleProps
 * @property {SortDirection} sortDirection - Dirección actual de ordenamiento.
 * @property {(direction: SortDirection) => void} onToggle - Callback al cambiar dirección.
 * @property {boolean} [disabled=false] - Deshabilita el botón.
 */
interface SortToggleProps {
  sortDirection: SortDirection;
  onToggle: (direction: SortDirection) => void;
  disabled?: boolean;
}

/**
 * Botón toggle para cambiar la dirección de ordenamiento por fecha.
 *
 * Alterna entre ordenamiento descendente (más recientes primero)
 * y ascendente (más antiguos primero). Al cambiar, invoca el callback
 * con la nueva dirección.
 *
 * Estados visuales:
 * - DESC activo: "↓ Más recientes"
 * - ASC activo: "↑ Más antiguos"
 *
 * @component
 * @param {SortToggleProps} props - Propiedades del componente.
 * @returns {JSX.Element} Botón de toggle de ordenamiento.
 *
 * @example
 * ```tsx
 * <SortToggle
 *   sortDirection="DESC"
 *   onToggle={(dir) => setSortDirection(dir)}
 * />
 * ```
 */
export const SortToggle: React.FC<SortToggleProps> = ({
  sortDirection,
  onToggle,
  disabled = false,
}) => {
  const handleClick = () => {
    const newDirection: SortDirection = sortDirection === 'DESC' ? 'ASC' : 'DESC';
    onToggle(newDirection);
  };

  const Icon = sortDirection === 'DESC' ? ArrowDown : ArrowUp;
  const label = sortDirection === 'DESC' ? 'Más recientes' : 'Más antiguos';

  return (
    <button
      onClick={handleClick}
      disabled={disabled}
      className="flex items-center gap-1.5 rounded-md border border-white/10 bg-zinc-800 px-3 py-1.5 text-sm text-zinc-300 transition-colors hover:bg-zinc-700 disabled:opacity-50"
      aria-label={`Ordenar por fecha: ${label}`}
      title={`Ordenar: ${label}`}
    >
      <Icon className="h-4 w-4" />
      <span>{label}</span>
    </button>
  );
};
