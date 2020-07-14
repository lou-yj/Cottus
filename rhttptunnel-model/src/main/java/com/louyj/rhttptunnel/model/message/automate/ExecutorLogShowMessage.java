package com.louyj.rhttptunnel.model.message.automate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ExecutorLogShowMessage extends BaseMessage {

	private String executor;

	private String task;

	private String scheduleId;

	@JsonCreator
	public ExecutorLogShowMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

}
