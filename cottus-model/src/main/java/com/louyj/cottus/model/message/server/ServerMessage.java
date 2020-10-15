package com.louyj.cottus.model.message.server;

import java.util.UUID;

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
public abstract class ServerMessage extends BaseMessage {

	private String serverMsgId = UUID.randomUUID().toString();

	@JsonCreator
	public ServerMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ServerMessage(ClientInfo client, String exchangeId, String serverMsgId) {
		super(client);
		setExchangeId(exchangeId);
		this.serverMsgId = serverMsgId;
	}

	public String getServerMsgId() {
		return serverMsgId;
	}

}
