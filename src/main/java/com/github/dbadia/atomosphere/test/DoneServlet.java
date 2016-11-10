package com.github.dbadia.atomosphere.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet which handles logout requests
 *
 * @author Dave Badia
 *
 */
@WebServlet(urlPatterns = { "/done" })
public class DoneServlet extends HttpServlet {
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		if (request.getCookies() != null) {
			for (final Cookie cookie : request.getCookies()) {
				cookie.setValue("");
				cookie.setPath("/");
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
		}
		request.getRequestDispatcher("done.jsp").forward(request, response);
	}
}
