package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ShellMessage extends BaseMessage {

	private String message;

	@JsonCreator
	public ShellMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ShellMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ShellMessage withMessage(String message) {
		setMessage(message);
		return this;
	}
}
