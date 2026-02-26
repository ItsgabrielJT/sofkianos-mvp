import React from 'react';
import { useKudosList } from '../hooks/data/useKudosList';
import {
  KudoTable,
  KudoFilters,
  KudoPagination,
  SortToggle,
  TableSkeleton,
  EmptyState,
  ErrorState,
} from '../components/kudos-list';

/**
 * Página principal de listado público de kudos.
 *
 * Container component que orquesta la lógica de obtención,
 * filtrado, paginación y ordenamiento de kudos. Delega la
 * presentación a componentes especializados.
 *
 * Estados manejados:
 * - **Loading**: Muestra TableSkeleton (shimmer effect)
 * - **Error**: Muestra ErrorState con opción de reintento
 * - **Empty**: Muestra EmptyState cuando no hay resultados
 * - **Success**: Muestra KudoTable + KudoPagination
 *
 * URL query params se preservan para deep linking (futuro).
 *
 * @component
 * @returns {JSX.Element} Página de exploración de kudos.
 *
 * @example
 * ```tsx
 * <Route path="/kudos/list" element={<KudosListPage />} />
 * ```
 */
const KudosListPage: React.FC = () => {
  const {
    data,
    isLoading,
    error,
    sortDirection,
    setFilters,
    setPage,
    setSortDirection,
    retry,
  } = useKudosList();

  const hasData = data && data.content.length > 0;
  const isEmpty = data && data.content.length === 0;

  return (
    <div className="min-h-screen">
      {/* Background gradient */}
      <div className="fixed inset-0 -z-10 bg-[radial-gradient(circle_at_top,_var(--tw-gradient-stops))] from-brand/5 via-zinc-950 to-zinc-950" />

      <div className="mx-auto max-w-7xl px-4 py-28 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight text-white sm:text-4xl">
            Explorar{' '}
            <span className="text-brand drop-shadow-[0_0_15px_rgba(255,95,0,0.5)]">
              Kudos
            </span>
          </h1>
          <p className="mt-2 text-lg text-zinc-400">
            Descubre los reconocimientos otorgados en la organización
          </p>
        </div>

        {/* Filtros */}
        <KudoFilters onFilter={setFilters} disabled={isLoading} />

        {/* Barra de acciones: Sort toggle + info */}
        <div className="mb-4 flex items-center justify-between">
          <SortToggle
            sortDirection={sortDirection}
            onToggle={setSortDirection}
            disabled={isLoading}
          />

          {data && (
            <p className="text-sm text-zinc-500">
              {data.totalElements}{' '}
              {data.totalElements === 1 ? 'kudo encontrado' : 'kudos encontrados'}
            </p>
          )}
        </div>

        {/* Contenido principal */}
        {isLoading && <TableSkeleton rows={10} />}

        {error && !isLoading && <ErrorState message={error} onRetry={retry} />}

        {isEmpty && !isLoading && !error && <EmptyState />}

        {hasData && !isLoading && !error && (
          <>
            <KudoTable items={data.content} />
            <KudoPagination
              currentPage={data.currentPage}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              size={data.size}
              onPageChange={setPage}
            />
          </>
        )}
      </div>
    </div>
  );
};

export default KudosListPage;
