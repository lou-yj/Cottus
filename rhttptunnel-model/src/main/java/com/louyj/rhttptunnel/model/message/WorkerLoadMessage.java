package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.worker.Workerload;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class WorkerLoadMessage extends BaseMessage {

	private Workerload workload;

	@JsonCreator
	public WorkerLoadMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public WorkerLoadMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public Workerload getWorkload() {
		return workload;
	}

	public void setWorkload(Workerload workload) {
		this.workload = workload;
	}

}
