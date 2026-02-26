import { describe, it, expect } from 'vitest';
import { formatDate, truncateText } from '../../utils/formatters';

describe('formatDate', () => {
  it('formatea un timestamp ISO 8601 correctamente', () => {
    const result = formatDate('2026-02-19T10:30:00Z');
    expect(result).toContain('19');
    expect(result).toContain('2026');
  });

  it('retorna "Fecha inválida" para strings no parseables', () => {
    expect(formatDate('not-a-date')).toBe('Fecha inválida');
  });

  it('retorna "Fecha inválida" para string vacío', () => {
    expect(formatDate('')).toBe('Fecha inválida');
  });

  it('formatea timestamps con diferentes zonas horarias', () => {
    const result = formatDate('2026-01-15T12:00:00Z');
    expect(result).toContain('2026');
    expect(result).toContain('15');
  });
});

describe('truncateText', () => {
  it('no trunca textos cortos', () => {
    expect(truncateText('Hola mundo')).toBe('Hola mundo');
  });

  it('trunca textos que exceden maxLength y agrega "..."', () => {
    const longText = 'A'.repeat(150);
    const result = truncateText(longText, 100);
    expect(result.length).toBe(103); // 100 + "..."
    expect(result.endsWith('...')).toBe(true);
  });

  it('no trunca textos exactamente en el límite', () => {
    const exactText = 'A'.repeat(100);
    expect(truncateText(exactText, 100)).toBe(exactText);
  });

  it('usa maxLength=100 por defecto', () => {
    const text = 'A'.repeat(101);
    const result = truncateText(text);
    expect(result.length).toBe(103);
  });

  it('respeta maxLength personalizado', () => {
    const result = truncateText('Hola mundo cruel', 4);
    expect(result).toBe('Hola...');
  });
});
