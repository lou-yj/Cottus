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
public class PwdMessage extends BaseMessage {

	@JsonCreator
	public PwdMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

}
