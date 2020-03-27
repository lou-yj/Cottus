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

	private String workdir;

	private String path;

	private String args;

	@JsonCreator
	public ExecMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ExecMessage(ClientInfo client, String path, String args) {
		super(client);
		this.path = path;
		this.args = args;
	}

	public String getWorkdir() {
		return workdir;
	}

	public void setWorkdir(String workdir) {
		this.workdir = workdir;
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
