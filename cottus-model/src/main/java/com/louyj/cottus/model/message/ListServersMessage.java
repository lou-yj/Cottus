package com.louyj.cottus.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ListServersMessage extends BaseMessage {

	@JsonCreator
	public ListServersMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ListServersMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

}
