package com.louyj.rhttptunnel.model.message;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class LsMessage extends BaseMessage {

	private String path;

	@JsonCreator
	public LsMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public LsMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public static LsMessage cack(ClientInfo client, String exchangeId) {
		return new LsMessage(client, exchangeId);
	}

	public static LsMessage sack(String exchangeId) {
		return new LsMessage(SERVER, exchangeId);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
