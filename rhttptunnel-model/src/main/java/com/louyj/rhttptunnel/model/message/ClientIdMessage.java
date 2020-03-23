package com.louyj.rhttptunnel.model.message;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ClientIdMessage extends BaseMessage {

	private Set<String> clientIds;

	@JsonCreator
	public ClientIdMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ClientIdMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public Set<String> getClientIds() {
		return clientIds;
	}

	public void setClientIds(Set<String> clientIds) {
		this.clientIds = Sets.newHashSet(clientIds);
	}

}
