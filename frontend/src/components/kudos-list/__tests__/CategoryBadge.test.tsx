import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CategoryBadge } from '../CategoryBadge';

describe('CategoryBadge', () => {
  it('renderiza el nombre de la categoría', () => {
    render(<CategoryBadge category="Innovation" />);
    expect(screen.getByText('Innovation')).toBeDefined();
  });

  it('aplica clase azul para Innovation', () => {
    const { container } = render(<CategoryBadge category="Innovation" />);
    const badge = container.querySelector('span');
    expect(badge?.className).toContain('text-blue-400');
    expect(badge?.className).toContain('bg-blue-500/20');
  });

  it('aplica clase verde para Teamwork', () => {
    const { container } = render(<CategoryBadge category="Teamwork" />);
    const badge = container.querySelector('span');
    expect(badge?.className).toContain('text-green-400');
  });

  it('aplica clase rojo para Passion', () => {
    const { container } = render(<CategoryBadge category="Passion" />);
    const badge = container.querySelector('span');
    expect(badge?.className).toContain('text-red-400');
  });

  it('aplica clase amarillo para Mastery', () => {
    const { container } = render(<CategoryBadge category="Mastery" />);
    const badge = container.querySelector('span');
    expect(badge?.className).toContain('text-yellow-400');
  });

  it('aplica estilo gris por defecto para categoría desconocida', () => {
    const { container } = render(<CategoryBadge category="Unknown" />);
    const badge = container.querySelector('span');
    expect(badge?.className).toContain('text-zinc-400');
  });
});
