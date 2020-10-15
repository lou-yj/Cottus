package com.louyj.cottus.model.message;

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

	private List<String> workerIds;

	@JsonCreator
	public SelectWorkerMessage(@JsonProperty("client") ClientInfo client,
			@JsonProperty("workers") List<String> workers) {
		super(client);
		this.workerIds = workers;
	}

	public List<String> getWorkerIds() {
		return workerIds;
	}

	public void setWorkerIds(List<String> workers) {
		this.workerIds = workers;
	}

}
