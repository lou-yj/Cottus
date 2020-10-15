package com.louyj.cottus.model.exception;

import com.louyj.cottus.model.message.consts.RejectReason;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class BadRequestException extends CustomException {

	private static final long serialVersionUID = 1L;

	private String description;

	public BadRequestException(String description) {
		super();
		this.description = description;
	}

	@Override
	public RejectReason status() {
		return RejectReason.BAD_REQUEST;
	}

	@Override
	public String description() {
		return description;
	}

}
