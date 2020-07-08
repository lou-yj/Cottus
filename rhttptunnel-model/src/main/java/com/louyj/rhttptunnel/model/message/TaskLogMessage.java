package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class TaskLogMessage extends BaseMessage {

	private String out;
	private String err;

	@JsonCreator
	public TaskLogMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public TaskLogMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

}
