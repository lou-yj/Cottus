package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ClientInfo {

	public static final ClientInfo SERVER = new ClientInfo("SERVER", "SERVER");

	@JsonCreator
	public ClientInfo(@JsonProperty("host") String host, @JsonProperty("ip") String ip) {
		super();
		this.host = host;
		this.ip = ip;
	}

	private String host;

	private String ip;

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
		return this.host + ":" + this.ip;
	}

	@Override
	public String toString() {
		return "[" + this.getClass().getSimpleName() + "]" + new Gson().toJson(this);
	}

}
