package com.louyj.cottus.model.message.file;

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
public class PwdMessage extends BaseMessage {

	@JsonCreator
	public PwdMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

}
