package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.RoleType;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class RoleMessage extends BaseMessage {

	private RoleType role;

	@JsonCreator
	public RoleMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public RoleMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public RoleType getRole() {
		return role;
	}

	public void setRole(RoleType role) {
		this.role = role;
	}

}
