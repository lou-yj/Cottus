package com.louyj.cottus.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.annotation.NoLogMessage;
import com.louyj.cottus.model.annotation.NoPermissionCheck;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@NoLogMessage
@NoPermissionCheck
public class ServerEventLongPullMessage extends BaseMessage {

	@JsonCreator
	public ServerEventLongPullMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ServerEventLongPullMessage(ClientInfo client, int second) {
		super(client);
	}

}
