package com.louyj.cottus.model.message.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class UserDelMessage extends BaseMessage {

	private String userName;

	@JsonCreator
	public UserDelMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public UserDelMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
