/**
 * Formatea un timestamp ISO 8601 a formato legible en español.
 *
 * Convierte una cadena ISO 8601 a formato "dd mes yyyy, HH:mm".
 * Ejemplo: "2026-02-19T10:30:00Z" → "19 feb 2026, 10:30"
 *
 * @function formatDate
 * @param {string} isoString - Timestamp en formato ISO 8601 UTC.
 * @returns {string} Fecha formateada en español. Retorna "Fecha inválida" si el input no es parseable.
 *
 * @example
 * formatDate("2026-02-19T10:30:00Z"); // "19 feb 2026, 10:30"
 */
export const formatDate = (isoString: string): string => {
  const date = new Date(isoString);
  if (isNaN(date.getTime())) return 'Fecha inválida';

  return date.toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });
};

/**
 * Trunca un texto a un máximo de caracteres, agregando "..." si excede.
 *
 * @function truncateText
 * @param {string} text - Texto original.
 * @param {number} [maxLength=100] - Longitud máxima antes de truncar.
 * @returns {string} Texto truncado con "..." o texto original si no excede.
 *
 * @example
 * truncateText("Un mensaje muy largo...", 20); // "Un mensaje muy larg..."
 */
export const truncateText = (text: string, maxLength: number = 100): string => {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength) + '...';
};
