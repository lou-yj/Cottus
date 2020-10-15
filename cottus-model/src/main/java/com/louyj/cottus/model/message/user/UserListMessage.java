package com.louyj.cottus.model.message.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.bean.User;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class UserListMessage extends BaseMessage {

	private List<User> users;

	@JsonCreator
	public UserListMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public UserListMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

}
