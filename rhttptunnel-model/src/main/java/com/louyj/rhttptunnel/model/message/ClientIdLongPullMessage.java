package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.annotation.NoLogMessage;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@NoLogMessage
public class ClientIdLongPullMessage extends BaseMessage {

	@JsonCreator
	public ClientIdLongPullMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ClientIdLongPullMessage(ClientInfo client, int second) {
		super(client);
	}

}
