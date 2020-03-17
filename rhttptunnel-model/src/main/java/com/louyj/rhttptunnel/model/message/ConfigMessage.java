package com.louyj.rhttptunnel.model.message;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ConfigMessage extends BaseMessage {

	private Map<String, String> config;

	@JsonCreator
	public ConfigMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}

}
