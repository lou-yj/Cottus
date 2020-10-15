package com.louyj.cottus.client.exception;

/**
 *
 * Create at 2020年7月1日
 *
 * @author Louyj
 *
 */
public class NoPermissionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String message = "Permission Denied";

	public NoPermissionException() {
		super();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
