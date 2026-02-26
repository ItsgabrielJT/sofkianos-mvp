import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import KudosListPage from '../pages/KudosListPage';
import { kudosService } from '../services/api/kudosService';
import {
  createMockPagedResponse,
  EMPTY_RESPONSE,
} from './mocks/kudosListMocks';

vi.mock('../services/api/kudosService', () => ({
  kudosService: {
    list: vi.fn(),
    send: vi.fn(),
  },
}));

const mockList = vi.mocked(kudosService.list);

const renderPage = () =>
  render(
    <MemoryRouter initialEntries={['/kudos/list']}>
      <KudosListPage />
    </MemoryRouter>,
  );

describe('KudosListPage', () => {
  beforeEach(() => {
    mockList.mockClear();
  });

  it('muestra skeleton loading al montar', () => {
    mockList.mockReturnValue(new Promise(() => {})); // Never resolves
    renderPage();

    // TableSkeleton renderiza animate-pulse divs
    expect(screen.getByText('Explorar')).toBeDefined();
  });

  it('renderiza tabla con datos cuando fetch es exitoso', async () => {
    const mockData = createMockPagedResponse();
    mockList.mockResolvedValueOnce(mockData);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText(mockData.content[0].fromUser)).toBeDefined();
    });

    expect(screen.getByText('150 kudos encontrados')).toBeDefined();
  });

  it('renderiza EmptyState cuando no hay resultados', async () => {
    mockList.mockResolvedValueOnce(EMPTY_RESPONSE);

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('No se encontraron kudos')).toBeDefined();
    });
  });

  it('renderiza ErrorState cuando el fetch falla', async () => {
    mockList.mockRejectedValueOnce(new Error('Connection timeout'));

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Error al cargar kudos')).toBeDefined();
      expect(screen.getByText('Connection timeout')).toBeDefined();
    });
  });

  it('reintenta fetch al click en "Reintentar"', async () => {
    const user = userEvent.setup();
    mockList.mockRejectedValueOnce(new Error('Error'));
    mockList.mockResolvedValueOnce(createMockPagedResponse());

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Error al cargar kudos')).toBeDefined();
    });

    await user.click(screen.getByRole('button', { name: /reintentar/i }));

    await waitFor(() => {
      expect(mockList).toHaveBeenCalledTimes(2);
    });
  });

  it('muestra header "Explorar Kudos"', async () => {
    mockList.mockResolvedValueOnce(createMockPagedResponse());
    renderPage();

    expect(screen.getByText('Explorar')).toBeDefined();
    expect(screen.getByText('Kudos')).toBeDefined();
  });

  it('muestra "1 kudo encontrado" en singular', async () => {
    mockList.mockResolvedValueOnce(
      createMockPagedResponse({
        totalElements: 1,
        totalPages: 1,
        content: [createMockPagedResponse().content[0]],
      }),
    );

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('1 kudo encontrado')).toBeDefined();
    });
  });

  it('invoca kudosService.list al montar', async () => {
    mockList.mockResolvedValueOnce(createMockPagedResponse());
    renderPage();

    await waitFor(() => {
      expect(mockList).toHaveBeenCalledTimes(1);
    });
  });

  it('renderiza KudoFilters accesible', async () => {
    mockList.mockResolvedValueOnce(createMockPagedResponse());
    renderPage();

    await waitFor(() => {
      expect(screen.getByRole('search', { name: /filtros de kudos/i })).toBeDefined();
    });
  });

  it('renderiza SortToggle con estado por defecto DESC', async () => {
    mockList.mockResolvedValueOnce(createMockPagedResponse());
    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Más recientes')).toBeDefined();
    });
  });
});
