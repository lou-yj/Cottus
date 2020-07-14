package com.louyj.rhttptunnel.model.message.shell;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

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
