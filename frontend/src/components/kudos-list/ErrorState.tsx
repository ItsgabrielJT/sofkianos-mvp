import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

/**
 * Propiedades del componente ErrorState.
 *
 * @interface ErrorStateProps
 * @property {string} message - Mensaje de error a mostrar.
 * @property {() => void} onRetry - Callback para reintentar la operación fallida.
 */
interface ErrorStateProps {
  message: string;
  onRetry: () => void;
}

/**
 * Componente de estado de error con opción de reintento.
 *
 * Muestra un mensaje de error amigable con un botón para reintentar
 * la operación que falló. Ideal para errores de red o del servidor.
 *
 * @component
 * @param {ErrorStateProps} props - Propiedades del componente.
 * @returns {JSX.Element} Estado de error con botón de reintento.
 *
 * @example
 * ```tsx
 * {error && <ErrorState message={error} onRetry={retry} />}
 * ```
 */
export const ErrorState: React.FC<ErrorStateProps> = ({ message, onRetry }) => (
  <div className="flex flex-col items-center justify-center rounded-lg border border-red-500/20 bg-red-950/20 py-12">
    <AlertTriangle className="mb-4 h-10 w-10 text-red-400" />
    <h3 className="text-lg font-medium text-red-300">
      Error al cargar kudos
    </h3>
    <p className="mt-1 max-w-md text-center text-sm text-red-400/80">
      {message}
    </p>
    <button
      onClick={onRetry}
      className="mt-4 flex items-center gap-2 rounded-md bg-red-500/20 px-4 py-2 text-sm font-medium text-red-300 transition-colors hover:bg-red-500/30"
    >
      <RefreshCw className="h-4 w-4" />
      Reintentar
    </button>
  </div>
);
