import React from 'react';

/**
 * Propiedades del componente SkeletonRow.
 *
 * @interface SkeletonRowProps
 * @property {number} [columns=5] - Número de columnas del skeleton.
 */
interface SkeletonRowProps {
  columns?: number;
}

/**
 * Fila skeleton para loading state de la tabla.
 *
 * Renderiza bloques animados que simulan contenido mientras se cargan datos.
 *
 * @component
 * @param {SkeletonRowProps} props - Propiedades del componente.
 * @returns {JSX.Element} Fila de tabla con animación skeleton.
 */
const SkeletonRow: React.FC<SkeletonRowProps> = ({ columns = 5 }) => (
  <tr className="border-b border-white/5">
    {Array.from({ length: columns }).map((_, i) => (
      <td key={i} className="p-3">
        <div className="h-4 rounded bg-zinc-800 animate-pulse" />
      </td>
    ))}
  </tr>
);

/**
 * Propiedades del componente TableSkeleton.
 *
 * @interface TableSkeletonProps
 * @property {number} [rows=5] - Número de filas skeleton a mostrar.
 */
interface TableSkeletonProps {
  rows?: number;
}

/**
 * Tabla skeleton completa para loading state.
 *
 * Simula la estructura de la tabla de kudos con filas animadas
 * mientras se espera la respuesta del backend.
 *
 * @component
 * @param {TableSkeletonProps} props - Propiedades del componente.
 * @returns {JSX.Element} Tabla completa con skeleton loading.
 *
 * @example
 * ```tsx
 * {isLoading && <TableSkeleton rows={10} />}
 * ```
 */
export const TableSkeleton: React.FC<TableSkeletonProps> = ({ rows = 5 }) => (
  <div className="overflow-x-auto rounded-lg border border-white/10">
    <table className="w-full min-w-[640px]">
      <thead>
        <tr className="border-b border-white/10 bg-zinc-900/50">
          {['De', 'Para', 'Categoría', 'Mensaje', 'Fecha'].map((header) => (
            <th
              key={header}
              scope="col"
              className="p-3 text-left text-xs font-medium uppercase tracking-wider text-zinc-500"
            >
              {header}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {Array.from({ length: rows }).map((_, i) => (
          <SkeletonRow key={i} />
        ))}
      </tbody>
    </table>
  </div>
);
