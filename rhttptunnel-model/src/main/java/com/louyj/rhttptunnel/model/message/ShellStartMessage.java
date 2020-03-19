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
public class ShellStartMessage extends BaseMessage {

	@JsonCreator
	public ShellStartMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ShellStartMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

}
