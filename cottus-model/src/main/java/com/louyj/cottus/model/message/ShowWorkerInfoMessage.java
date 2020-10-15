package com.louyj.cottus.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ShowWorkerInfoMessage extends BaseMessage {

	@JsonCreator
	public ShowWorkerInfoMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

}
