package com.louyj.rhttptunnel.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorLog;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ExecutorLogMessage extends BaseMessage {

	private List<ExecutorLog> logs;

	@JsonCreator
	public ExecutorLogMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ExecutorLogMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<ExecutorLog> getLogs() {
		return logs;
	}

	public void setLogs(List<ExecutorLog> logs) {
		this.logs = logs;
	}

}
