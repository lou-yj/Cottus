package com.louyj.rhttptunnel.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ServersMessage extends BaseMessage {

	private List<ClientInfo> servers = Lists.newArrayList();

	private String master;

	@JsonCreator
	public ServersMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ServersMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public List<ClientInfo> getServers() {
		return servers;
	}

	public void setServers(List<ClientInfo> servers) {
		this.servers = servers;
	}

}
