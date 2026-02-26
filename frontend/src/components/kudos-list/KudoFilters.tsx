import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Search, X } from 'lucide-react';
import type { KudoFilters as KudoFiltersType } from '../../types/kudos.types';
import { KUDO_CATEGORIES } from '../../schemas/kudoFormSchema';

/** Tiempo de debounce en milisegundos para la búsqueda de texto. */
const DEBOUNCE_MS = 500;

/**
 * Propiedades del componente KudoFilters.
 *
 * @interface KudoFiltersProps
 * @property {(filters: KudoFiltersType) => void} onFilter - Callback invocado al aplicar filtros.
 * @property {boolean} [disabled=false] - Deshabilita los campos mientras se carga la lista.
 */
interface KudoFiltersProps {
  onFilter: (filters: KudoFiltersType) => void;
  disabled?: boolean;
}

/**
 * Componente de filtros interactivos para la lista de kudos.
 *
 * Renderiza un formulario con:
 * - Input de búsqueda con debounce (500ms)
 * - Selector de categoría
 * - Date pickers para rango de fechas
 * - Botones de aplicar y limpiar filtros
 *
 * La búsqueda de texto se aplica automáticamente con debounce.
 * Los demás filtros se aplican al hacer submit del formulario.
 *
 * Validaciones:
 * - Si startDate > endDate, muestra error visual
 *
 * @component
 * @param {KudoFiltersProps} props - Propiedades del componente.
 * @returns {JSX.Element} Formulario de filtros responsivo.
 *
 * @example
 * ```tsx
 * <KudoFilters onFilter={handleFilter} disabled={isLoading} />
 * ```
 */
export const KudoFilters: React.FC<KudoFiltersProps> = ({
  onFilter,
  disabled = false,
}) => {
  const [category, setCategory] = useState<string>('');
  const [searchText, setSearchText] = useState<string>('');
  const [debouncedSearch, setDebouncedSearch] = useState<string>('');
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [dateError, setDateError] = useState<string>('');

  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Debounce para searchText
  useEffect(() => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    debounceTimer.current = setTimeout(() => {
      setDebouncedSearch(searchText);
    }, DEBOUNCE_MS);

    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
    };
  }, [searchText]);

  // Validación de fechas
  useEffect(() => {
    if (startDate && endDate && startDate > endDate) {
      setDateError('La fecha de inicio no puede ser posterior a la fecha de fin');
    } else {
      setDateError('');
    }
  }, [startDate, endDate]);

  const buildFilters = useCallback((): KudoFiltersType => {
    const filters: KudoFiltersType = {};

    if (category) {
      filters.category = category as KudoFiltersType['category'];
    }
    if (debouncedSearch.trim()) {
      filters.searchText = debouncedSearch.trim();
    }
    if (startDate) {
      filters.startDate = startDate;
    }
    if (endDate) {
      filters.endDate = endDate;
    }

    return filters;
  }, [category, debouncedSearch, startDate, endDate]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (dateError) return;
    onFilter(buildFilters());
  };

  const handleClear = () => {
    setCategory('');
    setSearchText('');
    setDebouncedSearch('');
    setStartDate('');
    setEndDate('');
    setDateError('');
    onFilter({});
  };

  const hasActiveFilters = category || searchText || startDate || endDate;

  return (
    <form
      onSubmit={handleSubmit}
      className="mb-6 rounded-lg border border-white/10 bg-zinc-900/50 p-4 sm:p-6"
      role="search"
      aria-label="Filtros de kudos"
    >
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {/* Búsqueda de texto */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-zinc-500" />
          <input
            type="text"
            placeholder="Buscar en de, para, mensaje..."
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            disabled={disabled}
            className="w-full rounded-md border border-white/10 bg-zinc-800 py-2 pl-10 pr-3 text-sm text-zinc-100 placeholder-zinc-500 transition-colors focus:border-brand focus:outline-none focus:ring-1 focus:ring-brand disabled:opacity-50"
            aria-label="Buscar kudos"
          />
        </div>

        {/* Selector de categoría */}
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          disabled={disabled}
          className="w-full rounded-md border border-white/10 bg-zinc-800 px-3 py-2 text-sm text-zinc-100 transition-colors focus:border-brand focus:outline-none focus:ring-1 focus:ring-brand disabled:opacity-50"
          aria-label="Filtrar por categoría"
        >
          <option value="">Todas las categorías</option>
          {KUDO_CATEGORIES.map((cat) => (
            <option key={cat} value={cat}>
              {cat}
            </option>
          ))}
        </select>

        {/* Fecha desde */}
        <div>
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            disabled={disabled}
            className="w-full rounded-md border border-white/10 bg-zinc-800 px-3 py-2 text-sm text-zinc-100 transition-colors focus:border-brand focus:outline-none focus:ring-1 focus:ring-brand disabled:opacity-50"
            aria-label="Fecha desde"
          />
        </div>

        {/* Fecha hasta */}
        <div>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            disabled={disabled}
            className={`w-full rounded-md border bg-zinc-800 px-3 py-2 text-sm text-zinc-100 transition-colors focus:outline-none focus:ring-1 disabled:opacity-50 ${
              dateError
                ? 'border-red-500 focus:border-red-500 focus:ring-red-500'
                : 'border-white/10 focus:border-brand focus:ring-brand'
            }`}
            aria-label="Fecha hasta"
            aria-invalid={!!dateError}
          />
          {dateError && (
            <p className="mt-1 text-xs text-red-400" role="alert">
              {dateError}
            </p>
          )}
        </div>
      </div>

      {/* Botones */}
      <div className="mt-4 flex flex-wrap items-center gap-3">
        <button
          type="submit"
          disabled={disabled || !!dateError}
          className="rounded-md bg-brand px-5 py-2 text-sm font-medium text-white transition-colors hover:bg-brand-hover disabled:opacity-50"
        >
          Aplicar Filtros
        </button>

        {hasActiveFilters && (
          <button
            type="button"
            onClick={handleClear}
            disabled={disabled}
            className="flex items-center gap-1.5 rounded-md border border-white/10 bg-zinc-800 px-4 py-2 text-sm text-zinc-300 transition-colors hover:bg-zinc-700 disabled:opacity-50"
          >
            <X className="h-3.5 w-3.5" />
            Limpiar
          </button>
        )}
      </div>
    </form>
  );
};
