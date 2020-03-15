package com.louyj.rhttptunnel.model.exception;

import com.louyj.rhttptunnel.model.message.status.RejectReason;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public abstract class CustomException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public abstract RejectReason status();

	public abstract String description();

}
