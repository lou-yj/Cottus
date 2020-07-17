package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月17日
 *
 * @author Louyj
 *
 */
public class NotifyMessage extends BaseMessage {

	private String message;

	@JsonCreator
	public NotifyMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public NotifyMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
