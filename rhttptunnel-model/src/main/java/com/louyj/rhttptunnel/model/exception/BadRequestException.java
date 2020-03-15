package com.louyj.rhttptunnel.model.exception;

import static com.louyj.rhttptunnel.model.message.status.RejectReason.BAD_REQUEST;

import com.louyj.rhttptunnel.model.message.status.RejectReason;

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
		return BAD_REQUEST;
	}

	@Override
	public String description() {
		return description;
	}

}
