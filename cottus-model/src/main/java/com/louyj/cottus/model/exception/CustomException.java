package com.louyj.cottus.model.exception;

import com.louyj.cottus.model.message.consts.RejectReason;

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
