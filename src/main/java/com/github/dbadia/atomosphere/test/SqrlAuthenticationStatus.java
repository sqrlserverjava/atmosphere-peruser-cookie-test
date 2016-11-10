package com.github.dbadia.atomosphere.test;

public enum SqrlAuthenticationStatus {
	CORRELATOR_ISSUED, SENDING, RECEIVING, COMMUNICATING, AUTH_COMPLETE;

	public boolean isErrorStatus() {
		return this.toString().startsWith("ERROR_");
	}

}
