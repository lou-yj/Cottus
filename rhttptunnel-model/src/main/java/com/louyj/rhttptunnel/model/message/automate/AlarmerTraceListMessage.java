package com.louyj.rhttptunnel.model.message.automate;

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
public class AlarmerTraceListMessage extends BaseMessage {

	private String uuid;

	@JsonCreator
	public AlarmerTraceListMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
