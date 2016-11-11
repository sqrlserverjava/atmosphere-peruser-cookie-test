package com.github.dbadia.atomosphere.test;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/start" })
public class StartServlet extends HttpServlet {
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final Cookie cookie = new Cookie(OurAtmosphereHandler.COOKIE_NAME, UUID.randomUUID().toString());
		final String domain = computeCookieDomain(request);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setMaxAge(60);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
		request.getRequestDispatcher("WEB-INF/start.jsp").forward(request, response);
	}

	public static String computeCookieDomain(final HttpServletRequest request) {
		final String requestUrl = request.getRequestURL().toString();
		String domain = requestUrl.substring(requestUrl.indexOf("//") + 2);
		domain = domain.substring(0, domain.indexOf('/'));
		final int portIndex = domain.indexOf(':');
		if (portIndex > -1) {
			domain = domain.substring(0, portIndex);
		}
		if ("localhost".equals(domain)) {
			// Browsers don't like localhost domain on a cookie
			// http://stackoverflow.com/questions/1134290/cookies-on-localhost-with-explicit-domain
			return null;
		}
		return domain;
	}

}
