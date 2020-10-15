package com.louyj.cottus.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.louyj.cottus.model.bean.ServerInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ServersMessage extends BaseMessage {

	private List<ServerInfo> servers = Lists.newArrayList();

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

	public List<ServerInfo> getServers() {
		return servers;
	}

	public void setServers(List<ServerInfo> servers) {
		this.servers = servers;
	}

}
