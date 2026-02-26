import React, { useState, useEffect } from "react";
import { Menu, X, ArrowRight } from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";

/**
 * Propiedades del componente Navbar.
 *
 * @interface NavbarProps
 * @property {(id: string) => void} [onNavigateToSection] - Callback opcional para navegar a secciones específicas de la página.
 */
export interface NavbarProps {
  onNavigateToSection?: (id: string) => void;
}

/**
 * Componente de barra de navegación sticky con soporte responsive y menú móvil.
 *
 * Renderiza una barra de navegación fija en la parte superior que cambia su apariencia
 * al hacer scroll. Incluye navegación entre vistas (Landing/App), enlaces a secciones
 * y un menú hamburguesa para dispositivos móviles. El componente adapta su contenido
 * según la ruta actual ('/kudos' para vista de aplicación, '/' para landing).
 *
 * @component
 * @param {NavbarProps} props - Propiedades del componente.
 * @param {(id: string) => void} [props.onNavigateToSection] - Función callback para manejar navegación a secciones.
 *        Si no se proporciona, usa scroll nativo a elementos por ID.
 *
 * @returns {JSX.Element} Elemento header con navegación responsive.
 *
 * @example
 * ```tsx
 * // Uso en landing page con callback personalizado
 * <Navbar onNavigateToSection={(id) => scrollToSection(id)} />
 *
 * // Uso básico sin callback (usa scroll nativo)
 * <Navbar />
 * ```
 */
export const Navbar: React.FC<NavbarProps> = ({ onNavigateToSection }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const isAppView = location.pathname === "/kudos" || location.pathname === "/kudos/list";
  const isListView = location.pathname === "/kudos/list";

  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => setIsScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  /**
   * Maneja la navegación a una sección específica de la página.
   *
   * Decide la estrategia de navegación según la ruta actual:
   * - Si no está en landing ('/'), navega a la landing con el hash de sección
   * - Si está en landing y existe callback, lo ejecuta
   * - Si está en landing sin callback, hace scroll nativo al elemento
   *
   * @param {string} id - ID de la sección destino o 'top' para ir al inicio.
   * @returns {void}
   */
  const handleNavClick = (id: string) => {
    if (location.pathname !== "/") {
      navigate("/" + (id === "top" ? "" : "#" + id));
    } else if (onNavigateToSection) {
      onNavigateToSection(id);
    } else {
      // Internal anchor logic if onLanding but no explicit callback
      const element = document.getElementById(id);
      if (element) {
        element.scrollIntoView({ behavior: "smooth" });
      } else if (id === "top") {
        window.scrollTo({ top: 0, behavior: "smooth" });
      }
    }
    setMobileMenuOpen(false);
  };

  /**
   * Alterna entre la vista de aplicación (/kudos) y la landing page (/).
   *
   * Navega a la ruta opuesta a la actual y cierra el menú móvil si está abierto.
   *
   * @returns {void}
   */
  const handleToggleView = () => {
    if (isAppView) {
      navigate("/");
    } else {
      navigate("/kudos");
    }
    setMobileMenuOpen(false);
  };

  return (
    <header
      className={`
        fixed top-0 z-50 w-full
        transition-all duration-300
        ${
          isScrolled || mobileMenuOpen
            ? "bg-zinc-950/90 backdrop-blur-md border-b border-white/10"
            : "bg-transparent"
        }
      `}
    >
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-6 lg:px-8">
        <button
          onClick={() => handleNavClick("top")}
          className="text-xl font-black tracking-tight text-white"
        >
          Sofkian
          <span className="text-white">OS</span>
          <span className="text-brand animate-pulse">_</span>
        </button>

        <nav className="hidden md:flex items-center text-sm">
          {!isAppView && (
            <>
              <button
                onClick={() => handleNavClick("como-funciona")}
                className="px-4 text-zinc-400 hover:text-white transition-colors"
              >
                Cómo funciona
              </button>

              <div className="h-5 w-px bg-white/10" />

              <button
                onClick={() => handleNavClick("tecnologia")}
                className="px-4 text-zinc-400 hover:text-white transition-colors"
              >
                Tecnología
              </button>

              <div className="h-5 w-px bg-white/10" />
            </>
          )}

          <button
            onClick={() => { navigate("/kudos/list"); setMobileMenuOpen(false); }}
            className={`px-4 transition-colors ${
              isListView
                ? "text-brand font-semibold"
                : "text-zinc-400 hover:text-white"
            }`}
          >
            Explorar Kudos
          </button>

          <div className="h-5 w-px bg-white/10" />

          <button
            onClick={handleToggleView}
            className="
              flex items-center gap-2 px-4
              text-white font-semibold
              hover:text-brand transition-colors
            "
          >
            {isAppView ? "Volver" : "Acceder"}
          </button>
        </nav>

        <button
          className="md:hidden text-zinc-300 hover:text-white"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          {mobileMenuOpen ? <X /> : <Menu />}
        </button>
      </div>

      {mobileMenuOpen && (
        <div className="md:hidden border-t border-white/10 bg-zinc-950/95 backdrop-blur-md px-6 py-6">
          <div className="flex flex-col gap-4 text-sm">
            {!isAppView && (
              <>
                <button
                  onClick={() => handleNavClick("como-funciona")}
                  className="text-left text-zinc-400 hover:text-white"
                >
                  Cómo funciona
                </button>
                <button
                  onClick={() => handleNavClick("tecnologia")}
                  className="text-left text-zinc-400 hover:text-white"
                >
                  Tecnología
                </button>
              </>
            )}

            <button
              onClick={() => { navigate("/kudos/list"); setMobileMenuOpen(false); }}
              className={`text-left ${
                isListView
                  ? "text-brand font-semibold"
                  : "text-zinc-400 hover:text-white"
              }`}
            >
              Explorar Kudos
            </button>

            <button
              onClick={handleToggleView}
              className="mt-4 flex items-center gap-2 text-white font-semibold hover:text-brand"
            >
              {isAppView ? "Volver" : "Acceder"}
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}
    </header>
  );
};
