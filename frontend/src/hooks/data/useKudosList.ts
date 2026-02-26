import { useState, useEffect, useCallback, useRef } from 'react';
import { kudosService } from '../../services';
import type {
  KudoFilters,
  PagedKudoResponse,
  SortDirection,
} from '../../types/kudos.types';

/** Tamaño de página por defecto. */
const DEFAULT_PAGE_SIZE = 20;

/**
 * Estado retornado por el hook useKudosList.
 *
 * @interface UseKudosListReturn
 * @property {PagedKudoResponse | null} data - Respuesta paginada del backend.
 * @property {boolean} isLoading - True mientras se ejecuta el fetch.
 * @property {string | null} error - Mensaje de error si el fetch falló.
 * @property {KudoFilters} filters - Filtros activos actualmente.
 * @property {number} page - Página actual (0-indexed).
 * @property {SortDirection} sortDirection - Dirección de ordenamiento actual.
 * @property {(filters: KudoFilters) => void} setFilters - Actualiza filtros y resetea a página 0.
 * @property {(page: number) => void} setPage - Cambia a la página indicada.
 * @property {(direction: SortDirection) => void} setSortDirection - Cambia dirección de orden y resetea página.
 * @property {() => void} retry - Reintenta el último fetch fallido.
 */
interface UseKudosListReturn {
  data: PagedKudoResponse | null;
  isLoading: boolean;
  error: string | null;
  filters: KudoFilters;
  page: number;
  sortDirection: SortDirection;
  setFilters: (filters: KudoFilters) => void;
  setPage: (page: number) => void;
  setSortDirection: (direction: SortDirection) => void;
  retry: () => void;
}

/**
 * Hook que gestiona el estado completo del listado de kudos.
 *
 * Encapsula la lógica de:
 * - Fetch de datos con filtros, paginación y ordenamiento
 * - Manejo de estados loading/error/data
 * - Reset automático a página 0 al cambiar filtros o sort
 * - Función de reintento tras error
 *
 * El fetch se ejecuta automáticamente al montar el componente y
 * cada vez que cambian filters, page o sortDirection.
 *
 * @function useKudosList
 * @returns {UseKudosListReturn} Estado y acciones del listado de kudos.
 *
 * @example
 * ```tsx
 * const { data, isLoading, error, setFilters, setPage, retry } = useKudosList();
 * ```
 */
export const useKudosList = (): UseKudosListReturn => {
  const [data, setData] = useState<PagedKudoResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFiltersState] = useState<KudoFilters>({});
  const [page, setPageState] = useState<number>(0);
  const [sortDirection, setSortDirectionState] = useState<SortDirection>('DESC');

  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchKudos = useCallback(async () => {
    // Cancelar fetch previo si existe
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    setIsLoading(true);
    setError(null);

    try {
      const result = await kudosService.list(
        filters,
        page,
        DEFAULT_PAGE_SIZE,
        sortDirection,
      );
      setData(result);
    } catch (err) {
      // No reportar errores de cancelación
      if (err instanceof Error && err.name === 'CanceledError') return;
      const message =
        err instanceof Error ? err.message : 'Error al cargar los kudos';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  }, [filters, page, sortDirection]);

  useEffect(() => {
    fetchKudos();

    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [fetchKudos]);

  const setFilters = useCallback((newFilters: KudoFilters) => {
    setFiltersState(newFilters);
    setPageState(0);
  }, []);

  const setPage = useCallback((newPage: number) => {
    setPageState(newPage);
  }, []);

  const setSortDirection = useCallback((direction: SortDirection) => {
    setSortDirectionState(direction);
    setPageState(0);
  }, []);

  const retry = useCallback(() => {
    fetchKudos();
  }, [fetchKudos]);

  return {
    data,
    isLoading,
    error,
    filters,
    page,
    sortDirection,
    setFilters,
    setPage,
    setSortDirection,
    retry,
  };
};
