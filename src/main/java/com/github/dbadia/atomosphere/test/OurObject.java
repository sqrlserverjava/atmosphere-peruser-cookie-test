package com.github.dbadia.atomosphere.test;

public class OurObject {
	/**
	 * Once the atmosphere reosurce is invalid, the correlatorCookieId may not be available, so persist it here
	 */
	private final String correlatorCookieId;

	public String getCorrelatorCookieId() {
		return correlatorCookieId;
	}

	/**
	 * The atmosphere UUID of the post data request that sent the correlator. Only useful for tracking
	 */
	private final String dataResourceUuid;
	private long triggerAt = computeTriggerAt();
	private long wait = 0;
	private SqrlAuthenticationStatus status = SqrlAuthenticationStatus.CORRELATOR_ISSUED;

	public OurObject(final String dataResourceUuid, final String cookieCorrelatorId) {
		super();
		this.dataResourceUuid = dataResourceUuid;
		this.correlatorCookieId = cookieCorrelatorId;
		if (cookieCorrelatorId == null) {
			throw new IllegalArgumentException("cookieCorrelatorId cannot be null");
		}
	}

	public String getDataResourceUuid() {
		return dataResourceUuid;
	}

	private long computeTriggerAt() {
		return System.currentTimeMillis() + wait;
	}

	public long incrementStatusAndResetTime() {
		final SqrlAuthenticationStatus toReturn = status;
		if (toReturn.ordinal() < SqrlAuthenticationStatus.values().length - 1) {
			status = SqrlAuthenticationStatus.values()[toReturn.ordinal() + 1];
			wait += 2000;
			triggerAt = computeTriggerAt();
		}
		return triggerAt;
	}

	public SqrlAuthenticationStatus getStatus() {
		return status;
	}

	public long getTriggerAt() {
		return triggerAt;
	}
}
