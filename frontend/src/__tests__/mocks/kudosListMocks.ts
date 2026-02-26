import type { KudoListItem, PagedKudoResponse } from '../../types/kudos.types';

/**
 * Genera un item de kudo para testing.
 *
 * @param {Partial<KudoListItem>} overrides - Campos a sobreescribir.
 * @param {number} index - Índice para generar datos únicos.
 * @returns {KudoListItem} Item de kudo con datos de prueba.
 */
export const createMockKudoItem = (
  overrides: Partial<KudoListItem> = {},
  index: number = 0,
): KudoListItem => ({
  id: `hash-${index}`,
  fromUser: `u***${index}@sofkianos.com`,
  toUser: `d***${index}@sofkianos.com`,
  category: ['Innovation', 'Teamwork', 'Passion', 'Mastery'][index % 4],
  message: `Excelente trabajo en el proyecto ${index}`,
  createdAt: new Date(2026, 1, 19, 10, 30 + index).toISOString(),
  ...overrides,
});

/**
 * Genera una lista de kudos mock.
 *
 * @param {number} count - Cantidad de items a generar.
 * @returns {KudoListItem[]} Lista de items mock.
 */
export const createMockKudoList = (count: number): KudoListItem[] =>
  Array.from({ length: count }, (_, i) => createMockKudoItem({}, i));

/**
 * Genera una respuesta paginada mock.
 *
 * @param {Partial<PagedKudoResponse>} overrides - Campos a sobreescribir.
 * @returns {PagedKudoResponse} Respuesta paginada mock.
 */
export const createMockPagedResponse = (
  overrides: Partial<PagedKudoResponse> = {},
): PagedKudoResponse => ({
  content: createMockKudoList(20),
  totalElements: 150,
  totalPages: 8,
  currentPage: 0,
  size: 20,
  ...overrides,
});

/**
 * Respuesta vacía para testing de empty state.
 */
export const EMPTY_RESPONSE: PagedKudoResponse = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  currentPage: 0,
  size: 20,
};
