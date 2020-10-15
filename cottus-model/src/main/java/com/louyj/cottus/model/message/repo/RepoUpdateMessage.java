package com.louyj.cottus.model.message.repo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class RepoUpdateMessage extends BaseMessage {

	private String message;

	@JsonCreator
	public RepoUpdateMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RepoUpdateMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public RepoUpdateMessage withMessage(String message) {
		setMessage(message);
		return this;
	}

}
