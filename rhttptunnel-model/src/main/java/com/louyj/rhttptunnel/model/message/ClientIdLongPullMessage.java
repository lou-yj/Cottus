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
public class ClientIdLongPullMessage extends BaseMessage {

	private int second = 10;

	@JsonCreator
	public ClientIdLongPullMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ClientIdLongPullMessage(ClientInfo client, int second) {
		super(client);
		this.second = second;
	}

	public int getSecond() {
		return second;
	}

}