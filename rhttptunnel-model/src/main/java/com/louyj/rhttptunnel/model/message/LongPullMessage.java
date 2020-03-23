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
public class LongPullMessage extends BaseMessage {

	private String clientId;

	private int second = 10;

	@JsonCreator
	public LongPullMessage(@JsonProperty("client") ClientInfo client, @JsonProperty("clientId") String clientId) {
		super(client);
		this.clientId = clientId;
	}

	public LongPullMessage(ClientInfo client, String clientId, int second) {
		super(client);
		this.second = second;
		this.clientId = clientId;
	}

	public int getSecond() {
		return second;
	}

	public String getClientId() {
		return clientId;
	}

}
