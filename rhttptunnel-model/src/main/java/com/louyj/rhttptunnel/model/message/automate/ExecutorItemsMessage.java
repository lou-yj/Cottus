package com.louyj.rhttptunnel.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ExecutorItemsMessage extends BaseMessage {

	private List<Executor> executors;

	@JsonCreator
	public ExecutorItemsMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ExecutorItemsMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<Executor> getExecutors() {
		return executors;
	}

	public void setExecutors(List<Executor> executors) {
		this.executors = executors;
	}

}
