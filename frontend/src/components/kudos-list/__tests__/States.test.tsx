import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EmptyState } from '../EmptyState';
import { ErrorState } from '../ErrorState';

describe('EmptyState', () => {
  it('muestra mensaje "No se encontraron kudos"', () => {
    render(<EmptyState />);
    expect(screen.getByText('No se encontraron kudos')).toBeDefined();
  });

  it('muestra texto de sugerencia', () => {
    render(<EmptyState />);
    expect(screen.getByText(/intenta ajustar los filtros/i)).toBeDefined();
  });
});

describe('ErrorState', () => {
  it('muestra el mensaje de error', () => {
    render(<ErrorState message="Connection timeout" onRetry={vi.fn()} />);
    expect(screen.getByText('Connection timeout')).toBeDefined();
  });

  it('muestra título "Error al cargar kudos"', () => {
    render(<ErrorState message="Error" onRetry={vi.fn()} />);
    expect(screen.getByText('Error al cargar kudos')).toBeDefined();
  });

  it('invoca onRetry al click en "Reintentar"', async () => {
    const user = userEvent.setup();
    const onRetry = vi.fn();
    render(<ErrorState message="Error" onRetry={onRetry} />);

    await user.click(screen.getByRole('button', { name: /reintentar/i }));
    expect(onRetry).toHaveBeenCalledTimes(1);
  });
});
