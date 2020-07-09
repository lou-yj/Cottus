package com.louyj.rhttptunnel.model.message.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class HandlerScheduleMessage extends TaskScheduleMessage {

	@JsonCreator
	public HandlerScheduleMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public HandlerScheduleMessage(ClientInfo client, String exchangeId, String serverMsgId) {
		super(client, exchangeId, serverMsgId);
	}

}
