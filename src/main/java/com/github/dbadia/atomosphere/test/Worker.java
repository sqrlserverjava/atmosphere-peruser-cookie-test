package com.github.dbadia.atomosphere.test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Worker.class);
	private static final String EOL = System.getProperty("line.separator");
	private static final Object MONITOR_TABLE_LOCK = new Object();
	private final Map<Long, OurObject> monitorTable = new ConcurrentHashMap<>();
	private final Map<String, AtmosphereResource> currentResourceTable = new ConcurrentHashMap<>();

	@Override
	public void run() {
		try {
			synchronized (MONITOR_TABLE_LOCK) {
				final Iterator<Long> iter = monitorTable.keySet().iterator();
				while (iter.hasNext()) {
					final Long time = iter.next();
					if (System.currentTimeMillis() > time.longValue()) {
						final OurObject ourObject = monitorTable.remove(time);
						final String cookiecorrelatorCookieId = ourObject.getCorrelatorCookieId();
						if (cookiecorrelatorCookieId == null) {
							logger.error("Got null cookiecorrelatorCookieId");
						} else {
							final AtmosphereResource resource = currentResourceTable.get(cookiecorrelatorCookieId);
							if (resource != null) {
								if (resource.isCancelled()) {
									// The user is on another page or a new request will come in

									if (currentResourceTable.get(cookiecorrelatorCookieId).equals(resource)) {
										currentResourceTable.remove(cookiecorrelatorCookieId);
									}
								}
								sendAtmostphereResponse(ourObject, ourObject.getStatus());
								if (ourObject.getStatus().ordinal() < SqrlAuthenticationStatus.values().length - 1) {
									monitorTable.put(ourObject.incrementStatusAndResetTime(), ourObject);
								}
							}
						}
					}
				}
			}
		} catch (final Throwable t) {
			// Don't let worker thread die
			logger.error("Caught error in " + getClass().getSimpleName() + ".run()", t);
		}
	}

	public void sendAtmostphereResponse(final OurObject ourObject,
			final SqrlAuthenticationStatus newAuthStatus) {
		final String cookiecorrelatorCookieId = ourObject.getCorrelatorCookieId();
		final AtmosphereResource resource = currentResourceTable.get(cookiecorrelatorCookieId);
		if (resource == null) {
			logger.error("AtmosphereResource not found for sessionId {}", cookiecorrelatorCookieId);
			return;
		}
		final AtmosphereResponse res = resource.getResponse();
		res.setContentType("application/json");
		// @formatter:off
		/*
		 * In general, the Atmosphere documentation recommends using the
		 * Broadcaster interface. However, we use the the
		 * raw resource api instead for the following reasons:
		 *
		 * 1. We need a 1 to 1 broadcast approach. Attempts to do so
		 * 		with 2.5.4 did not work, when we called broadcast,
		 * 		onStateChange was never invoked
		 * 2. Even if the above did work, each broadcaster spawns at least 3
		 * 		threads via executor service.  There are ways around that
		 * 		but it just seems like needless overhead
		 */
		// @formatter:on
		try {
			final String data = "{ \"author:\":\"" + ourObject.getCorrelatorCookieId() + "\", \"message\" : \""
					+ newAuthStatus.toString() + "\" }";
			res.getWriter().write(data);
			switch (resource.transport()) {
				case JSONP:
				case LONG_POLLING:
					resource.resume();
					break;
				case WEBSOCKET:
					break;
				case SSE: // this is not in the original examples but is necessary for SSE
				case STREAMING:
					res.getWriter().flush();
					break;
				default:
					throw new IOException("Don't know how to handle transport " + resource.transport());
			}
			logger.info("Status update of {} sent to correlator cookie {}", newAuthStatus, cookiecorrelatorCookieId);
		} catch (final IOException e) {
			logger.error("Error sending status update to correlator cookie " + cookiecorrelatorCookieId, e);
		}
	}

	public void stopMonitoringCorrelatorCookieId(final String cookiecorrelatorCookieId) {
		synchronized (MONITOR_TABLE_LOCK) {
			final Iterator<OurObject> iter = monitorTable.values().iterator();
			while (iter.hasNext()) {
				final OurObject clientObject = iter.next();
				if (clientObject.getCorrelatorCookieId().equals(cookiecorrelatorCookieId)) {
					iter.remove();
					return;
				}
			}
		}
		logger.error("Tried to remove cookiecorrelatorCookieId {} from monitorTable but it wasn't there",
				cookiecorrelatorCookieId);
	}

	public void monitorCorrelatorForChange(final OurObject object) {
		synchronized (MONITOR_TABLE_LOCK) {
			monitorTable.put(object.getTriggerAt(), object);
		}
	}

	public void storeLatestResource(final AtmosphereResource resource) {
		final String correlatorCookieId = OurAtmosphereHandler.extractCookieId(resource);
		if (correlatorCookieId == null) {
			logger.error("correlator cookie id was null ");
		} else {
			logger.debug("Updating current resource to {} for correlator cookie {}", resource.uuid(),
					correlatorCookieId);
			currentResourceTable.put(correlatorCookieId, resource);
		}
	}

}
