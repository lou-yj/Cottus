package com.louyj.rhttptunnel.model.message.file;

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
public class PwdMessage extends BaseMessage {

	@JsonCreator
	public PwdMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

}
