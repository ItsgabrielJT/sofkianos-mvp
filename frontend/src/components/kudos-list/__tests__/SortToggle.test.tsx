import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SortToggle } from '../SortToggle';

describe('SortToggle', () => {
  it('muestra "Más recientes" cuando sortDirection=DESC', () => {
    render(<SortToggle sortDirection="DESC" onToggle={vi.fn()} />);
    expect(screen.getByText('Más recientes')).toBeDefined();
  });

  it('muestra "Más antiguos" cuando sortDirection=ASC', () => {
    render(<SortToggle sortDirection="ASC" onToggle={vi.fn()} />);
    expect(screen.getByText('Más antiguos')).toBeDefined();
  });

  it('invoca onToggle con "ASC" al click desde DESC', async () => {
    const user = userEvent.setup();
    const onToggle = vi.fn();
    render(<SortToggle sortDirection="DESC" onToggle={onToggle} />);

    await user.click(screen.getByRole('button'));
    expect(onToggle).toHaveBeenCalledWith('ASC');
  });

  it('invoca onToggle con "DESC" al click desde ASC', async () => {
    const user = userEvent.setup();
    const onToggle = vi.fn();
    render(<SortToggle sortDirection="ASC" onToggle={onToggle} />);

    await user.click(screen.getByRole('button'));
    expect(onToggle).toHaveBeenCalledWith('DESC');
  });

  it('deshabilita el botón cuando disabled=true', () => {
    render(<SortToggle sortDirection="DESC" onToggle={vi.fn()} disabled />);
    expect((screen.getByRole('button') as HTMLButtonElement).disabled).toBe(true);
  });

  it('tiene aria-label descriptiva', () => {
    render(<SortToggle sortDirection="DESC" onToggle={vi.fn()} />);
    const btn = screen.getByRole('button');
    expect(btn.getAttribute('aria-label')).toContain('Más recientes');
  });
});
