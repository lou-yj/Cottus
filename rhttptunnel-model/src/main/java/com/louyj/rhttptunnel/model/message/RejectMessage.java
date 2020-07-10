package com.louyj.rhttptunnel.model.message;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class RejectMessage extends BaseMessage {

	private String reason;

	@JsonCreator
	public RejectMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RejectMessage(ClientInfo client, String reason) {
		super(client);
		this.reason = reason;
	}

	public RejectMessage(ClientInfo client, String reason, String exchangeId) {
		super(client);
		this.reason = reason;
		setExchangeId(exchangeId);
	}

	public String getReason() {
		return reason;
	}

	public static RejectMessage creason(ClientInfo client, String exchangeId, String reason) {
		return new RejectMessage(client, reason, exchangeId);
	}

	public static RejectMessage sreason(String exchangeId, String reason) {
		return new RejectMessage(SERVER, reason, exchangeId);
	}

}
