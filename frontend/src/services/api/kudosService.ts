import { apiClient } from "./client";
import type { KudoFormData } from "../../schemas/kudoFormSchema";
import type {
  KudoFilters,
  PagedKudoResponse,
  SortDirection,
} from "../../types/kudos.types";

/**
 * Servicio que encapsula todas las operaciones relacionadas con Kudos.
 *
 * Proporciona métodos para interactuar con los endpoints de kudos del backend.
 * Usa el cliente HTTP centralizado {@link apiClient} para todas las peticiones.
 *
 * Métodos disponibles:
 * - **send**: Envía un kudo al backend (POST /v1/kudos)
 * - **list**: Obtiene listado paginado de kudos (GET /v1/kudos)
 *
 * Características:
 * - Validación de status code esperado (202 Accepted para send)
 * - Construcción dinámica de query params para filtros
 * - Manejo de errores mediante excepciones
 * - Tipado fuerte con TypeScript
 * - Integración con esquemas de validación
 *
 * @namespace kudosService
 */
export const kudosService = {
  /**
   * Envía un kudo al backend para ser procesado.
   *
   * Realiza una petición POST al endpoint /v1/kudos con los datos del kudo.
   * El backend responde con 202 Accepted indicando que el kudo fue recibido
   * y será procesado asincrónicamente por el consumer worker.
   *
   * Flujo:
   * 1. Envía POST a /v1/kudos con payload
   * 2. Backend valida datos y publica evento en RabbitMQ
   * 3. Responde 202 Accepted si todo es correcto
   * 4. Consumer worker procesa el kudo asincrónicamente
   *
   * Códigos de respuesta:
   * - **202 Accepted**: Kudo recibido correctamente
   * - **400 Bad Request**: Datos de validación incorrectos (manejado por interceptor)
   * - **500 Internal Server Error**: Error del servidor (manejado por interceptor)
   *
   * @async
   * @function send
   * @memberof kudosService
   * @param {KudoFormData} payload - Datos del kudo a enviar.
   * @param {string} payload.from - Email del remitente.
   * @param {string} payload.to - Email del destinatario.
   * @param {string} payload.category - Categoría del kudo.
   * @param {string} payload.message - Mensaje del kudo (10-500 caracteres).
   * @returns {Promise<void>} Promesa que se resuelve cuando el kudo es aceptado.
   * @throws {Error} Si el status code no es 202.
   * @throws {AxiosError} Si hay error de red o el servidor responde con error.
   *
   * @example
   * await kudosService.send({
   *   from: 'sender@sofkianos.com',
   *   to: 'recipient@sofkianos.com',
   *   category: 'Colaboración',
   *   message: 'Excelente trabajo en el proyecto X'
   * });
   */
  send: async (payload: KudoFormData): Promise<void> => {
    const response = await apiClient.post("/v1/kudos", payload);
    if (response.status !== 202) {
      throw new Error(`Unexpected status: ${response.status}`);
    }
  },

  /**
   * Obtiene un listado paginado de kudos desde el backend.
   *
   * Realiza una petición GET al endpoint /v1/kudos construyendo
   * los query parameters a partir de los filtros, página, tamaño
   * y dirección de ordenamiento proporcionados.
   *
   * Los filtros opcionales se agregan al query string solo si están presentes.
   * Los parámetros obligatorios (page, size, sortDirection) siempre se envían.
   *
   * Flujo:
   * 1. Construye URLSearchParams con parámetros base
   * 2. Agrega filtros opcionales si están definidos
   * 3. Envía GET a /v1/kudos?{params}
   * 4. Retorna respuesta tipada con datos paginados
   *
   * @async
   * @function list
   * @memberof kudosService
   * @param {KudoFilters} [filters={}] - Filtros opcionales de búsqueda.
   * @param {number} [page=0] - Número de página (0-indexed).
   * @param {number} [size=20] - Cantidad de items por página (máx 50).
   * @param {SortDirection} [sortDirection='DESC'] - Dirección de ordenamiento por fecha.
   * @returns {Promise<PagedKudoResponse>} Respuesta paginada con kudos y metadata.
   * @throws {AxiosError} Si hay error de red o el servidor responde con error.
   *
   * @example
   * // Sin filtros
   * const response = await kudosService.list();
   *
   * @example
   * // Con filtros y paginación
   * const response = await kudosService.list(
   *   { category: 'Teamwork', searchText: 'proyecto' },
   *   0,
   *   20,
   *   'DESC'
   * );
   */
  list: async (
    filters: KudoFilters = {},
    page: number = 0,
    size: number = 20,
    sortDirection: SortDirection = "DESC",
  ): Promise<PagedKudoResponse> => {
    const params = new URLSearchParams();
    params.append("page", page.toString());
    params.append("size", size.toString());
    params.append("sortDirection", sortDirection);

    if (filters.category) params.append("category", filters.category);
    if (filters.searchText) params.append("searchText", filters.searchText);
    if (filters.startDate) params.append("startDate", filters.startDate);
    if (filters.endDate) params.append("endDate", filters.endDate);

    const response = await apiClient.get<PagedKudoResponse>(
      `/v1/kudos?${params.toString()}`,
    );
    return response.data;
  },
};

export default kudosService;
