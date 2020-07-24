package com.louyj.rhttptunnel.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.annotation.NoPermissionCheck;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@NoPermissionCheck
public class RegistryMessage extends BaseMessage {

	private ClientInfo registryClient;

	private List<String> servers;

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

	public List<String> getServers() {
		return servers;
	}

	public void setServers(List<String> servers) {
		this.servers = servers;
	}

}
