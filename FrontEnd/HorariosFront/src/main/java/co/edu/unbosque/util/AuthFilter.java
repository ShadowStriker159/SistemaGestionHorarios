package co.edu.unbosque.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = { "/secretaria/*", "/docente/*", "/estudiante/*" })
public class AuthFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		HttpSession session = request.getSession(false);
		String rolSesion = (session == null) ? null : (String) session.getAttribute("usuarioRol");

		String path = request.getServletPath();
		String rolEsperado;
		if (path.startsWith("/docente")) {
			rolEsperado = "docente";
		} else if (path.startsWith("/estudiante")) {
			rolEsperado = "estudiante";
		} else {
			rolEsperado = "administrador"; // /secretaria
		}

		if (rolSesion == null || !rolSesion.equals(rolEsperado)) {
			response.sendRedirect(request.getContextPath() + "/index.xhtml");
			return;
		}

		chain.doFilter(req, res);
	}
}