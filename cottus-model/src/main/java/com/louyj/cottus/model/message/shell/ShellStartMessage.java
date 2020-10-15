package com.louyj.cottus.model.message.shell;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

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
