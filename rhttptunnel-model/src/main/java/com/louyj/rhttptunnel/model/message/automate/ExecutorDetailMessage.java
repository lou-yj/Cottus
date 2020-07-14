package com.louyj.rhttptunnel.model.message.automate;

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
public class ExecutorDetailMessage extends BaseMessage {

	private Executor executor;

	@JsonCreator
	public ExecutorDetailMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ExecutorDetailMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

}
