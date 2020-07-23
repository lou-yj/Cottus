package com.louyj.rhttptunnel.model.message;

import java.util.List;

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

	private List<String> servers;

	private String publicKey;

	private String aesKey;

	@JsonCreator
	public RegistryMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RegistryMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
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
