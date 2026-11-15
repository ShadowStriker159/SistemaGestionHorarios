package co.edu.unbosque.util;

import co.edu.unbosque.model.UsuarioSesionDTO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;

public class AutorizacionFilter implements Filter {

	public static final String SESION_USUARIO = "usuarioSesion";

	@Override
	public void init(FilterConfig filterConfig) {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String contextPath = httpRequest.getContextPath();
		String requestPath = httpRequest.getRequestURI().substring(contextPath.length());

		if (requestPath.startsWith("/jakarta.faces.resource/") || requestPath.startsWith("/javax.faces.resource/")) {
			chain.doFilter(request, response);
			return;
		}

		if (!requestPath.toLowerCase(Locale.ROOT).endsWith(".xhtml")) {
			chain.doFilter(request, response);
			return;
		}

		HttpSession session = httpRequest.getSession(false);
		Object usuario = session == null ? null : session.getAttribute(SESION_USUARIO);
		String pagina = requestPath.toLowerCase(Locale.ROOT);

		if ("/index.xhtml".equals(pagina)) {
			if (usuario instanceof UsuarioSesionDTO sesion && rolValido(sesion.getRol())) {
				httpResponse.sendRedirect(contextPath + paginaInicio(sesion.getRol()));
				return;
			}
			chain.doFilter(request, response);
			return;
		}

		if (!(usuario instanceof UsuarioSesionDTO sesion) || !rolValido(sesion.getRol())) {
			if (session != null) {
				try {
					session.invalidate();
				} catch (IllegalStateException ignored) {
					
				}
			}
			httpResponse.sendRedirect(contextPath + "/index.xhtml");
			return;
		}

		String rol = normalizarRol(sesion.getRol());
		if (pagina.startsWith("/secretaria/") && !"administrador".equals(rol)
				|| pagina.startsWith("/docente/") && !"docente".equals(rol)
				|| pagina.startsWith("/estudiante/") && !"estudiante".equals(rol) || !pagina.startsWith("/secretaria/")
						&& !pagina.startsWith("/docente/") && !pagina.startsWith("/estudiante/")) {
			httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para acceder a esta página.");
			return;
		}

		chain.doFilter(request, response);
	}

	public static boolean rolValido(String rol) {
		String normalizado = normalizarRol(rol);
		return "administrador".equals(normalizado) || "docente".equals(normalizado) || "estudiante".equals(normalizado);
	}

	public static String normalizarRol(String rol) {
		if (rol == null) {
			return "";
		}
		String normalizado = rol.trim().toLowerCase(Locale.ROOT);
		return switch (normalizado) {
		case "admin", "secretaria" -> "administrador";
		default -> normalizado;
		};
	}

	public static String paginaInicio(String rol) {
		return switch (normalizarRol(rol)) {
		case "administrador" -> "/secretaria/dashboard.xhtml";
		case "docente" -> "/docente/dashboard.xhtml";
		case "estudiante" -> "/estudiante/dashboard.xhtml";
		default -> "/index.xhtml";
		};
	}

	@Override
	public void destroy() {
	}
}