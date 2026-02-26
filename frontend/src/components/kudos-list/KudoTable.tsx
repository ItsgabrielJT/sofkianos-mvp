import React from 'react';
import type { KudoListItem } from '../../types/kudos.types';
import { CategoryBadge } from './CategoryBadge';
import { formatDate, truncateText } from '../../utils/formatters';

/**
 * Propiedades del componente KudoTable.
 *
 * @interface KudoTableProps
 * @property {KudoListItem[]} items - Lista de kudos a mostrar en la tabla.
 */
interface KudoTableProps {
  items: KudoListItem[];
}

/**
 * Tabla presentacional de kudos con columnas semánticas.
 *
 * Renderiza una tabla HTML accesible con los kudos proporcionados.
 * Incluye:
 * - Columnas: De, Para, Categoría, Mensaje, Fecha
 * - Badges de color por categoría
 * - Fechas formateadas en español
 * - Mensajes truncados a 100 caracteres
 * - Responsive con scroll horizontal en mobile
 * - HTML semántico con `scope="col"` para accesibilidad
 *
 * @component
 * @param {KudoTableProps} props - Propiedades del componente.
 * @returns {JSX.Element} Tabla con lista de kudos.
 *
 * @example
 * ```tsx
 * <KudoTable items={kudosList} />
 * ```
 */
export const KudoTable: React.FC<KudoTableProps> = ({ items }) => {
  return (
    <div className="overflow-x-auto rounded-lg border border-white/10">
      <table className="w-full min-w-[640px]">
        <thead>
          <tr className="border-b border-white/10 bg-zinc-900/50">
            <th
              scope="col"
              className="p-3 text-left text-xs font-medium uppercase tracking-wider text-zinc-500"
            >
              De
            </th>
            <th
              scope="col"
              className="p-3 text-left text-xs font-medium uppercase tracking-wider text-zinc-500"
            >
              Para
            </th>
            <th
              scope="col"
              className="p-3 text-left text-xs font-medium uppercase tracking-wider text-zinc-500"
            >
              Categoría
            </th>
            <th
              scope="col"
              className="p-3 text-left text-xs font-medium uppercase tracking-wider text-zinc-500"
            >
              Mensaje
            </th>
            <th
              scope="col"
              className="p-3 text-left text-xs font-medium uppercase tracking-wider text-zinc-500"
            >
              Fecha
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-white/5">
          {items.map((item) => (
            <tr
              key={item.id}
              className="transition-colors hover:bg-zinc-900/40"
            >
              <td className="whitespace-nowrap p-3 text-sm text-zinc-300">
                {item.fromUser}
              </td>
              <td className="whitespace-nowrap p-3 text-sm text-zinc-300">
                {item.toUser}
              </td>
              <td className="whitespace-nowrap p-3 text-sm">
                <CategoryBadge category={item.category} />
              </td>
              <td
                className="max-w-xs p-3 text-sm text-zinc-400"
                title={item.message}
              >
                {truncateText(item.message)}
              </td>
              <td className="whitespace-nowrap p-3 text-sm text-zinc-500">
                {formatDate(item.createdAt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
