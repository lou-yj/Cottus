package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class UnSelectWorkerMessage extends BaseMessage {

	@JsonCreator
	public UnSelectWorkerMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

}
