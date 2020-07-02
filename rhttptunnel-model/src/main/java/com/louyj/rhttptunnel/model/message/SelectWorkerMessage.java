package com.louyj.rhttptunnel.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class SelectWorkerMessage extends BaseMessage {

	private List<ClientInfo> workers;

	@JsonCreator
	public SelectWorkerMessage(@JsonProperty("client") ClientInfo client,
			@JsonProperty("workers") List<ClientInfo> workers) {
		super(client);
		this.workers = workers;
	}

	public List<ClientInfo> getWorkers() {
		return workers;
	}

	public void setWorkers(List<ClientInfo> workers) {
		this.workers = workers;
	}

}
