import { useState, useEffect } from 'react';
import { kudosService } from '../../services/api/kudosService';
import type { PagedKudoResponse } from '../../models/kudoPublic';

/**
 * Hook personalizado para gestionar la obtención y paginación de kudos públicos.
 *
 * Proporciona una interfaz reactiva para interactuar con el endpoint de kudos públicos,
 * manejando automáticamente el estado de carga, errores y paginación.
 *
 * Funcionalidad:
 * - Carga automática de kudos al montar el componente
 * - Gestión de estados (loading, error, data)
 * - Paginación con navegación simple
 * - Recarga automática al cambiar de página
 *
 * Estado gestionado:
 * - **data**: Respuesta paginada con contenido de kudos
 * - **isLoading**: Indica si está cargando datos
 * - **error**: Mensaje de error si ocurre un fallo
 *
 * @function useKudosPublic
 * @returns {{
 *   data: PagedKudoResponse | null,
 *   isLoading: boolean,
 *   error: string | null,
 *   goToPage: (page: number) => void
 * }} Estado y funciones de control.
 * 
 */
export const useKudosPublic = () => {
  const [data, setData] = useState<PagedKudoResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState<number>(0);

  /**
   * Carga los kudos de la página especificada.
   * Se ejecuta automáticamente al montar y cuando cambia currentPage.
   */
  useEffect(() => {
    const fetchKudos = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const response = await kudosService.list({
          page: currentPage,
          size: 20,
          sortDirection: 'DESC',
        });
        setData(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Error desconocido');
      } finally {
        setIsLoading(false);
      }
    };

    fetchKudos();
  }, [currentPage]);

  /**
   * Navega a una página específica.
   *
   * @param {number} page - Número de página (base 0).
   */
  const goToPage = (page: number) => {
    if (page >= 0) {
      setCurrentPage(page);
    }
  };

  return {
    data,
    isLoading,
    error,
    goToPage,
  };
};
