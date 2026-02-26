import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

/** Máximo de botones de página visibles. */
const MAX_VISIBLE_PAGES = 5;

/**
 * Propiedades del componente KudoPagination.
 *
 * @interface KudoPaginationProps
 * @property {number} currentPage - Página actual (0-indexed).
 * @property {number} totalPages - Total de páginas disponibles.
 * @property {number} totalElements - Total de elementos en todas las páginas.
 * @property {number} [size=20] - Cantidad de items por página.
 * @property {(page: number) => void} onPageChange - Callback al cambiar de página.
 */
interface KudoPaginationProps {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size?: number;
  onPageChange: (page: number) => void;
}

/**
 * Calcula qué números de página mostrar con elipsis.
 *
 * Retorna un array de números de página y null para representar elipsis ("...").
 * Muestra máximo {@link MAX_VISIBLE_PAGES} botones numéricos y agrega elipsis
 * cuando hay más páginas de las visibles.
 *
 * @param {number} current - Página actual (0-indexed).
 * @param {number} total - Total de páginas.
 * @returns {(number | null)[]} Array de números de página y nulls para elipsis.
 */
const getPageNumbers = (current: number, total: number): (number | null)[] => {
  if (total <= MAX_VISIBLE_PAGES) {
    return Array.from({ length: total }, (_, i) => i);
  }

  const pages: (number | null)[] = [];
  const half = Math.floor(MAX_VISIBLE_PAGES / 2);

  let start = Math.max(0, current - half);
  let end = Math.min(total - 1, current + half);

  if (current - half < 0) {
    end = Math.min(total - 1, MAX_VISIBLE_PAGES - 1);
  }
  if (current + half > total - 1) {
    start = Math.max(0, total - MAX_VISIBLE_PAGES);
  }

  if (start > 0) {
    pages.push(0);
    if (start > 1) pages.push(null); // elipsis
  }

  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  if (end < total - 1) {
    if (end < total - 2) pages.push(null); // elipsis
    pages.push(total - 1);
  }

  return pages;
};

/**
 * Componente de paginación interactiva para la lista de kudos.
 *
 * Renderiza controles de navegación entre páginas incluyendo:
 * - Botones Anterior/Siguiente con estados disabled apropiados
 * - Números de página con elipsis para rangos grandes
 * - Indicador "Mostrando X-Y de Z kudos"
 * - Responsive: botones más compactos en mobile
 * - ARIA labels y role="navigation" para accesibilidad
 *
 * No se renderiza si totalPages <= 1.
 *
 * @component
 * @param {KudoPaginationProps} props - Propiedades del componente.
 * @returns {JSX.Element | null} Controles de paginación o null si solo hay 1 página.
 *
 * @example
 * ```tsx
 * <KudoPagination
 *   currentPage={0}
 *   totalPages={10}
 *   totalElements={195}
 *   size={20}
 *   onPageChange={(page) => setPage(page)}
 * />
 * ```
 */
export const KudoPagination: React.FC<KudoPaginationProps> = ({
  currentPage,
  totalPages,
  totalElements,
  size = 20,
  onPageChange,
}) => {
  if (totalPages <= 1) return null;

  const startItem = currentPage * size + 1;
  const endItem = Math.min((currentPage + 1) * size, totalElements);
  const pageNumbers = getPageNumbers(currentPage, totalPages);

  const isFirstPage = currentPage === 0;
  const isLastPage = currentPage === totalPages - 1;

  return (
    <nav
      className="mt-6 flex flex-col items-center gap-4 sm:flex-row sm:justify-between"
      role="navigation"
      aria-label="Paginación de kudos"
    >
      {/* Indicador de rango */}
      <p className="text-sm text-zinc-500">
        Mostrando{' '}
        <span className="font-medium text-zinc-300">
          {startItem}-{endItem}
        </span>{' '}
        de{' '}
        <span className="font-medium text-zinc-300">{totalElements}</span>{' '}
        kudos
      </p>

      {/* Controles de navegación */}
      <div className="flex items-center gap-1">
        {/* Botón Anterior */}
        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={isFirstPage}
          className="flex items-center gap-1 rounded-md border border-white/10 bg-zinc-800 px-3 py-1.5 text-sm text-zinc-300 transition-colors hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
          aria-label="Página anterior"
        >
          <ChevronLeft className="h-4 w-4" />
          <span className="hidden sm:inline">Anterior</span>
        </button>

        {/* Números de página */}
        {pageNumbers.map((pageNum, idx) =>
          pageNum === null ? (
            <span
              key={`ellipsis-${idx}`}
              className="px-2 text-sm text-zinc-500"
              aria-hidden="true"
            >
              …
            </span>
          ) : (
            <button
              key={pageNum}
              onClick={() => onPageChange(pageNum)}
              disabled={pageNum === currentPage}
              className={`min-w-[2rem] rounded-md px-2.5 py-1.5 text-sm font-medium transition-colors ${
                pageNum === currentPage
                  ? 'bg-brand text-white'
                  : 'border border-white/10 bg-zinc-800 text-zinc-300 hover:bg-zinc-700'
              }`}
              aria-label={`Ir a página ${pageNum + 1}`}
              aria-current={pageNum === currentPage ? 'page' : undefined}
            >
              {pageNum + 1}
            </button>
          ),
        )}

        {/* Botón Siguiente */}
        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={isLastPage}
          className="flex items-center gap-1 rounded-md border border-white/10 bg-zinc-800 px-3 py-1.5 text-sm text-zinc-300 transition-colors hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
          aria-label="Página siguiente"
        >
          <span className="hidden sm:inline">Siguiente</span>
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </nav>
  );
};
