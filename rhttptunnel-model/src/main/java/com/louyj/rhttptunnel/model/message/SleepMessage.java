package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
public class SleepMessage extends BaseMessage {

	private long second = 5;

	@JsonCreator
	public SleepMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public SleepMessage(ClientInfo client, long second) {
		super(client);
		this.second = second;
	}

	public long getSecond() {
		return second;
	}

}
