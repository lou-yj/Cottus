package com.louyj.cottus.model.message.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class TaskAckMessage extends ServerMessage {

	private TaskMetricsMessage.ExecuteStatus status;

	private String message;

	@JsonCreator
	public TaskAckMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public TaskAckMessage(ClientInfo client, String exchangeId, String serverMsgId) {
		super(client, exchangeId, serverMsgId);
	}

	public TaskMetricsMessage.ExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(TaskMetricsMessage.ExecuteStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
