package com.louyj.rhttptunnel.model.message.shell;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ShellResultMessage extends BaseMessage {

	private List<String> out = Collections.emptyList();
	private List<String> err = Collections.emptyList();

	@JsonCreator
	public ShellResultMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ShellResultMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<String> getOut() {
		return out;
	}

	public void setOut(List<String> out) {
		this.out = out;
	}

	public List<String> getErr() {
		return err;
	}

	public void setErr(List<String> err) {
		this.err = err;
	}

}
