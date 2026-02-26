import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { KudoPagination } from '../KudoPagination';

describe('KudoPagination', () => {
  const defaultProps = {
    currentPage: 0,
    totalPages: 8,
    totalElements: 150,
    size: 20,
    onPageChange: vi.fn(),
  };

  it('no renderiza nada si totalPages <= 1', () => {
    const { container } = render(
      <KudoPagination {...defaultProps} totalPages={1} totalElements={10} />,
    );
    expect(container.innerHTML).toBe('');
  });

  it('muestra indicador de rango "Mostrando X-Y de Z kudos"', () => {
    render(<KudoPagination {...defaultProps} />);
    expect(screen.getByText(/mostrando/i)).toBeDefined();
    expect(screen.getByText('1-20')).toBeDefined();
    expect(screen.getByText('150')).toBeDefined();
  });

  it('deshabilita botón "Anterior" en primera página', () => {
    render(<KudoPagination {...defaultProps} currentPage={0} />);
    const prevBtn = screen.getByLabelText('Página anterior');
    expect((prevBtn as HTMLButtonElement).disabled).toBe(true);
  });

  it('habilita botón "Anterior" en páginas posteriores', () => {
    render(<KudoPagination {...defaultProps} currentPage={3} />);
    const prevBtn = screen.getByLabelText('Página anterior');
    expect((prevBtn as HTMLButtonElement).disabled).toBe(false);
  });

  it('deshabilita botón "Siguiente" en última página', () => {
    render(<KudoPagination {...defaultProps} currentPage={7} />);
    const nextBtn = screen.getByLabelText('Página siguiente');
    expect((nextBtn as HTMLButtonElement).disabled).toBe(true);
  });

  it('habilita botón "Siguiente" en páginas anteriores a la última', () => {
    render(<KudoPagination {...defaultProps} currentPage={0} />);
    const nextBtn = screen.getByLabelText('Página siguiente');
    expect((nextBtn as HTMLButtonElement).disabled).toBe(false);
  });

  it('invoca onPageChange con página correcta al click "Siguiente"', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<KudoPagination {...defaultProps} currentPage={3} onPageChange={onPageChange} />);

    await user.click(screen.getByLabelText('Página siguiente'));
    expect(onPageChange).toHaveBeenCalledWith(4);
  });

  it('invoca onPageChange con página correcta al click "Anterior"', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<KudoPagination {...defaultProps} currentPage={3} onPageChange={onPageChange} />);

    await user.click(screen.getByLabelText('Página anterior'));
    expect(onPageChange).toHaveBeenCalledWith(2);
  });

  it('invoca onPageChange al click en un número de página', async () => {
    const user = userEvent.setup();
    const onPageChange = vi.fn();
    render(<KudoPagination {...defaultProps} onPageChange={onPageChange} />);

    const page2Button = screen.getByLabelText('Ir a página 2');
    await user.click(page2Button);
    expect(onPageChange).toHaveBeenCalledWith(1);
  });

  it('marca la página actual con aria-current="page"', () => {
    render(<KudoPagination {...defaultProps} currentPage={0} />);
    const currentPageBtn = screen.getByLabelText('Ir a página 1');
    expect(currentPageBtn.getAttribute('aria-current')).toBe('page');
  });

  it('tiene role="navigation" y aria-label', () => {
    render(<KudoPagination {...defaultProps} />);
    expect(
      screen.getByRole('navigation', { name: /paginación de kudos/i }),
    ).toBeDefined();
  });

  it('muestra elipsis para muchas páginas', () => {
    render(
      <KudoPagination
        {...defaultProps}
        currentPage={4}
        totalPages={20}
        totalElements={400}
      />,
    );
    const ellipses = screen.getAllByText('…');
    expect(ellipses.length).toBeGreaterThan(0);
  });

  it('calcula rango correcto para página intermedia', () => {
    render(
      <KudoPagination {...defaultProps} currentPage={2} totalElements={47} />,
    );
    expect(screen.getByText('41-47')).toBeDefined();
  });
});
