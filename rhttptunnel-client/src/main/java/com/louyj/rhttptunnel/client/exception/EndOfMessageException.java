package com.louyj.rhttptunnel.client.exception;

/**
 *
 * Create at 2020年7月1日
 *
 * @author Louyj
 *
 */
public class EndOfMessageException extends Exception {

	private static final long serialVersionUID = 1L;

	private String message;

	public EndOfMessageException(String message) {
		super();
		this.message = message;
	}

	public EndOfMessageException() {
		super();
	}

	public String getMessage() {
		return message;
	}

}
