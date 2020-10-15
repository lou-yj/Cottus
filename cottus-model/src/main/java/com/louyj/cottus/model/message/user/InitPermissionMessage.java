package com.louyj.cottus.model.message.user;

import java.util.Map;
import java.util.Set;

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
public class InitPermissionMessage extends BaseMessage {

	Map<String, Set<String>> commandGroups;

	@JsonCreator
	public InitPermissionMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public InitPermissionMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public Map<String, Set<String>> getCommandGroups() {
		return commandGroups;
	}

	public void setCommandGroups(Map<String, Set<String>> commandGroups) {
		this.commandGroups = commandGroups;
	}

}
