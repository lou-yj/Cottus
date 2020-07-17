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
public class RegistryMessage extends BaseMessage {

	private ClientInfo registryClient;

	@JsonCreator
	public RegistryMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RegistryMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public ClientInfo getRegistryClient() {
		return registryClient;
	}

	public void setRegistryClient(ClientInfo registryClient) {
		this.registryClient = registryClient;
	}

}
