package com.louyj.rhttptunnel.model.bean;

import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Create at 2020年7月1日
 *
 * @author Louyj
 *
 */
public class ServerInfo {

	private ClientInfo clientInfo;

	private String url;

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
