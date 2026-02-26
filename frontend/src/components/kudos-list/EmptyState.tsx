import React from 'react';
import { Inbox } from 'lucide-react';

/**
 * Componente de estado vacío para cuando no hay kudos que mostrar.
 *
 * Muestra un icono y mensaje amigable indicando que no se encontraron
 * resultados con los filtros actuales.
 *
 * @component
 * @returns {JSX.Element} Estado vacío con icono y mensaje.
 *
 * @example
 * ```tsx
 * {data.content.length === 0 && <EmptyState />}
 * ```
 */
export const EmptyState: React.FC = () => (
  <div className="flex flex-col items-center justify-center rounded-lg border border-white/10 bg-zinc-900/30 py-16">
    <Inbox className="mb-4 h-12 w-12 text-zinc-600" />
    <h3 className="text-lg font-medium text-zinc-400">
      No se encontraron kudos
    </h3>
    <p className="mt-1 text-sm text-zinc-500">
      Intenta ajustar los filtros o vuelve más tarde
    </p>
  </div>
);
