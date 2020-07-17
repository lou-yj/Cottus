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
public class ShowWorkerWorkloadMessage extends BaseMessage {

	@JsonCreator
	public ShowWorkerWorkloadMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

}
