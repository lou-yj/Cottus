package com.louyj.rhttptunnel.model.exception;

import com.louyj.rhttptunnel.model.message.MessageType;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class BadTypeException extends BadRequestException {

	private static final long serialVersionUID = 1L;

	private MessageType actual;

	private MessageType except;

	public BadTypeException(MessageType actual, MessageType except) {
		super("");
		this.actual = actual;
		this.except = except;
	}

	public MessageType getActual() {
		return actual;
	}

	public MessageType getExcept() {
		return except;
	}

	@Override
	public String description() {
		return "except " + except + " actual " + actual;
	}

}
