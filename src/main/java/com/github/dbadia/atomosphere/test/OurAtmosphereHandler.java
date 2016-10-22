package com.github.dbadia.atomosphere.test;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;

import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple AtmosphereHandler that sends status updates to the browser on a per client basis. Makes use of session to
 *
 * @author Dave Badia
 */

@AtmosphereHandlerService(path = "/update")
public class OurAtmosphereHandler implements AtmosphereHandler {
	private static final Logger logger = LoggerFactory.getLogger(OurAtmosphereHandler.class);
	public static final String COOKIE_NAME = "correlatorId";
	private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
	private final Worker processor;

	public OurAtmosphereHandler() {
		processor = new Worker();
		scheduledExecutor.scheduleAtFixedRate(processor, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	public void onRequest(final AtmosphereResource resource) throws IOException {
		final AtmosphereRequest request = resource.getRequest();

		// First, tell Atmosphere to allow bi-directional communication by suspending.
		if (request.getMethod().equalsIgnoreCase("GET")) {
			logger.info("Atmosphere onRequest {} {} {} {}", request.getMethod(), cookiesToString(request.getCookies()),
					resource.uuid(), request.getHeader("User-Agent"));
			resource.suspend();
			processor.storeLatestResource(resource);
		} else if (request.getMethod().equalsIgnoreCase("POST")) {
			// Post means we're being sent data
			final String message = request.getReader().readLine().trim();
			logger.info("Atmosphere onRequest {} {} {} {} {}", request.getMethod(),
					cookiesToString(request.getCookies()),
					resource.uuid(), message, request.getHeader("User-Agent"));

			// Message looks like { "author" : "foo", "message" : "bar" }
			final String author = message.substring(message.indexOf(":") + 2, message.indexOf(",") - 1);
			logger.debug("author = {}", author);
			final String messageText = message.substring(message.lastIndexOf(":") + 2, message.length() - 2);

			final String correlatorCookieId = extractCookieId(resource);
			if ("redirect".equals(messageText)) {
				// The browser received the complete update and is redirecting, clean up
				processor.stopMonitoringCorrelatorCookieId(correlatorCookieId);
			} else {
				// It's the initial correlator message
				final OurObject object = new OurObject(resource.uuid(), correlatorCookieId);
				processor.monitorCorrelatorForChange(object);
			}

		}
	}

	// We don't use broadcast so this is only called when a browser disconnects
	@Override
	public void onStateChange(final AtmosphereResourceEvent event) throws IOException {
		final AtmosphereResource resource = event.getResource();

		if (!event.isResuming()) {
			logger.info("Atmosphere browser closed connection for uuid {} correlatorCookieId {}", resource.uuid(),
					extractCookieId(resource));
		}
	}

	@Override
	public void destroy() {
	}

	private static final String cookiesToString(final Cookie[] cookieArray) {
		final StringBuilder buf = new StringBuilder("C[ ");
		for (final Cookie cookie : cookieArray) {
			buf.append(cookie.getName()).append("=").append(cookie.getValue()).append(", ");
		}

		return buf.substring(0, buf.length() - 2) + " ]";
	}

	public static String extractCookieId(final AtmosphereResource resource) {
		if(resource.getRequest() == null || resource.getRequest().getCookies() == null) {
			throw new IllegalStateException("Cookies not found on request");
		}
		for (final Cookie cookie : resource.getRequest().getCookies()) {
			if (COOKIE_NAME.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		throw new IllegalStateException("Cookie " + COOKIE_NAME + " not found on request");
	}

}