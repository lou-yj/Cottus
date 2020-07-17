package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AsyncFetchMessage extends BaseMessage {

	@JsonCreator
	public AsyncFetchMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

}
