package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.annotation.NoLogMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@NoLogMessage
public class NoContentMessage extends BaseMessage {

	@JsonCreator
	public NoContentMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public NoContentMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

}
