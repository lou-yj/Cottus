package com.louyj.rhttptunnel.model.message;

import java.util.UUID;

import org.joda.time.DateTime;

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

	private String uuid = UUID.randomUUID().toString();

	private String uptime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");

	private String host;

	private String ip;

	private String user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUptime() {
		return uptime;
	}

	public String getUuid() {
		return uuid;
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
		return this.uuid;
	}

	@Override
	public String toString() {
		return "[" + this.getClass().getSimpleName() + "]" + new Gson().toJson(this);
	}

}
