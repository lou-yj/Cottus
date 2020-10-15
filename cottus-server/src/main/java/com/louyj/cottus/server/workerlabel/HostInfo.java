package com.louyj.cottus.server.workerlabel;

public class HostInfo {

	private String host;

	private String ip;

	public HostInfo() {
		super();
	}

	public HostInfo(String host, String ip) {
		super();
		this.host = host;
		this.ip = ip;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String identify() {
		return host + ":" + ip;
	}

}
