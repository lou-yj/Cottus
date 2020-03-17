package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
public class ExecMessage extends BaseMessage {

	private String path;

	private String args;

	private int timeout;

	@JsonCreator
	public ExecMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ExecMessage(ClientInfo client, String path, String args, int timeout) {
		super(client);
		this.path = path;
		this.timeout = timeout;
		this.args = args;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

}
