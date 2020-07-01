package com.louyj.rhttptunnel.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class WorkerListMessage extends BaseMessage {

	private List<WorkerInfo> workers = Lists.newArrayList();

	@JsonCreator
	public WorkerListMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public WorkerListMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<WorkerInfo> getWorkers() {
		return workers;
	}

	public void setWorkers(List<WorkerInfo> workers) {
		this.workers = workers;
	}

}
