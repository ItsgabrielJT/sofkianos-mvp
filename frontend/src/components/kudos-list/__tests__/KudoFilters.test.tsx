import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { KudoFilters } from '../KudoFilters';

describe('KudoFilters', () => {
  const mockOnFilter = vi.fn();

  beforeEach(() => {
    mockOnFilter.mockClear();
  });

  it('renderiza todos los campos de filtro', () => {
    render(<KudoFilters onFilter={mockOnFilter} />);

    expect(screen.getByLabelText('Buscar kudos')).toBeDefined();
    expect(screen.getByLabelText('Filtrar por categoría')).toBeDefined();
    expect(screen.getByLabelText('Fecha desde')).toBeDefined();
    expect(screen.getByLabelText('Fecha hasta')).toBeDefined();
  });

  it('renderiza botón "Aplicar Filtros"', () => {
    render(<KudoFilters onFilter={mockOnFilter} />);
    expect(screen.getByRole('button', { name: /aplicar filtros/i })).toBeDefined();
  });

  it('invoca onFilter al hacer submit del form', async () => {
    const user = userEvent.setup();
    render(<KudoFilters onFilter={mockOnFilter} />);

    await user.click(screen.getByRole('button', { name: /aplicar filtros/i }));
    expect(mockOnFilter).toHaveBeenCalledWith({});
  });

  it('incluye categoría seleccionada en los filtros', async () => {
    const user = userEvent.setup();
    render(<KudoFilters onFilter={mockOnFilter} />);

    await user.selectOptions(screen.getByLabelText('Filtrar por categoría'), 'Teamwork');
    await user.click(screen.getByRole('button', { name: /aplicar filtros/i }));

    expect(mockOnFilter).toHaveBeenCalledWith(
      expect.objectContaining({ category: 'Teamwork' }),
    );
  });

  it('muestra botón "Limpiar" cuando hay filtros activos', async () => {
    const user = userEvent.setup();
    render(<KudoFilters onFilter={mockOnFilter} />);

    await user.selectOptions(screen.getByLabelText('Filtrar por categoría'), 'Innovation');

    expect(screen.getByRole('button', { name: /limpiar/i })).toBeDefined();
  });

  it('resetea todos los campos al hacer click en "Limpiar"', async () => {
    const user = userEvent.setup();
    render(<KudoFilters onFilter={mockOnFilter} />);

    await user.selectOptions(screen.getByLabelText('Filtrar por categoría'), 'Innovation');
    await user.click(screen.getByRole('button', { name: /limpiar/i }));

    expect(mockOnFilter).toHaveBeenCalledWith({});
    expect(
      (screen.getByLabelText('Filtrar por categoría') as HTMLSelectElement).value,
    ).toBe('');
  });

  it('muestra error si startDate > endDate', async () => {
    const user = userEvent.setup();
    render(<KudoFilters onFilter={mockOnFilter} />);

    await user.type(screen.getByLabelText('Fecha desde'), '2026-03-01');
    await user.type(screen.getByLabelText('Fecha hasta'), '2026-02-01');

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeDefined();
    });
  });

  it('deshabilita campos cuando disabled=true', () => {
    render(<KudoFilters onFilter={mockOnFilter} disabled={true} />);

    expect(
      (screen.getByLabelText('Buscar kudos') as HTMLInputElement).disabled,
    ).toBe(true);
    expect(
      (screen.getByLabelText('Filtrar por categoría') as HTMLSelectElement).disabled,
    ).toBe(true);
  });

  it('tiene role="search" y aria-label', () => {
    render(<KudoFilters onFilter={mockOnFilter} />);
    expect(screen.getByRole('search', { name: /filtros de kudos/i })).toBeDefined();
  });

  it('renderiza opciones de categoría correctas', () => {
    render(<KudoFilters onFilter={mockOnFilter} />);
    const select = screen.getByLabelText('Filtrar por categoría') as HTMLSelectElement;

    expect(select.options.length).toBe(5); // "Todas" + 4 categorías
    expect(select.options[0].textContent).toBe('Todas las categorías');
    expect(select.options[1].textContent).toBe('Innovation');
    expect(select.options[2].textContent).toBe('Teamwork');
    expect(select.options[3].textContent).toBe('Passion');
    expect(select.options[4].textContent).toBe('Mastery');
  });
});
