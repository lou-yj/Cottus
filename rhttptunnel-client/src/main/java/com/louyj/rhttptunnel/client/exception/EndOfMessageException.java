package com.louyj.rhttptunnel.client.exception;

import org.apache.commons.lang3.StringUtils;

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
		this.message = StringUtils.EMPTY;
	}

	public String getMessage() {
		return message;
	}

}
