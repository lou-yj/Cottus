package com.louyj.rhttptunnel.model.message.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ConfigGetMessage extends BaseMessage {

	private String key;

	@JsonCreator
	public ConfigGetMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
