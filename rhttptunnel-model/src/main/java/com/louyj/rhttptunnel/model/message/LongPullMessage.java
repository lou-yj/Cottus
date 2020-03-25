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

	@JsonCreator
	public LongPullMessage(@JsonProperty("client") ClientInfo client, @JsonProperty("clientId") String clientId) {
		super(client);
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

}
