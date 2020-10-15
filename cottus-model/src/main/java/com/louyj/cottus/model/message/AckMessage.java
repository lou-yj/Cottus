package com.louyj.cottus.model.message;

import static com.louyj.cottus.model.message.ClientInfo.SERVER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class AckMessage extends BaseMessage {

	private String message;

	@JsonCreator
	public AckMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public AckMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public static AckMessage cack(ClientInfo client, String exchangeId) {
		return new AckMessage(client, exchangeId);
	}

	public static AckMessage sack(String exchangeId) {
		return new AckMessage(SERVER, exchangeId);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public AckMessage withMessage(String message) {
		setMessage(message);
		return this;
	}

}
