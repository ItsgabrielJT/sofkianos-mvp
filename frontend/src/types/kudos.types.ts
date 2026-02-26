/**
 * Categorías válidas de Kudos en el sistema.
 *
 * @typedef {string} KudoCategory
 */
export type KudoCategory = 'Innovation' | 'Teamwork' | 'Passion' | 'Mastery';

/**
 * Filtros opcionales para la búsqueda de kudos.
 *
 * Todos los campos son opcionales. Si se envían múltiples filtros,
 * se aplican con lógica AND.
 *
 * @interface KudoFilters
 * @property {KudoCategory} [category] - Filtrar por categoría específica.
 * @property {string} [searchText] - Búsqueda full-text en fromUser, toUser, message.
 * @property {string} [startDate] - Fecha inicio del rango (ISO 8601).
 * @property {string} [endDate] - Fecha fin del rango (ISO 8601).
 */
export interface KudoFilters {
  category?: KudoCategory;
  searchText?: string;
  startDate?: string;
  endDate?: string;
}

/**
 * Item individual de kudo con datos enmascarados para privacidad.
 *
 * Los emails están enmascarados (ej: `j***z@domain.com`) y los IDs
 * son hashes, no secuenciales.
 *
 * @interface KudoListItem
 * @property {string} id - ID hasheado del kudo (no secuencial).
 * @property {string} fromUser - Email enmascarado del remitente.
 * @property {string} toUser - Email enmascarado del destinatario.
 * @property {string} category - Categoría del kudo.
 * @property {string} message - Mensaje del reconocimiento.
 * @property {string} createdAt - Timestamp ISO 8601 UTC.
 */
export interface KudoListItem {
  id: string;
  fromUser: string;
  toUser: string;
  category: string;
  message: string;
  createdAt: string;
}

/**
 * Respuesta paginada del endpoint GET /api/v1/kudos.
 *
 * Contiene la lista de kudos junto con metadata de paginación
 * para navegación en el frontend.
 *
 * @interface PagedKudoResponse
 * @property {KudoListItem[]} content - Lista de kudos en la página actual.
 * @property {number} totalElements - Total de kudos que coinciden con los filtros.
 * @property {number} totalPages - Total de páginas disponibles.
 * @property {number} currentPage - Página actual (0-indexed).
 * @property {number} size - Tamaño de página solicitado.
 */
export interface PagedKudoResponse {
  content: KudoListItem[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

/**
 * Dirección de ordenamiento para la lista de kudos.
 *
 * @typedef {'ASC' | 'DESC'} SortDirection
 */
export type SortDirection = 'ASC' | 'DESC';
