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

	private ClientInfo worker;

	@JsonCreator
	public UnSelectWorkerMessage(@JsonProperty("client") ClientInfo client, @JsonProperty("worker") ClientInfo worker) {
		super(client);
		this.worker = worker;
	}

	public ClientInfo getWorker() {
		return worker;
	}

	public void setWorker(ClientInfo worker) {
		this.worker = worker;
	}

}
