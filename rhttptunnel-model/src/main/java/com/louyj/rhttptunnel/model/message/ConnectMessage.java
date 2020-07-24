package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.annotation.NoPermissionCheck;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@NoPermissionCheck
public class ConnectMessage extends BaseMessage {

	private String user;

	private String password;

	private boolean superAdmin;

	@JsonCreator
	public ConnectMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ConnectMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

}
