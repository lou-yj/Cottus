package com.louyj.cottus.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.bean.automate.ExecutorLog;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;
import com.louyj.cottus.model.message.server.TaskScheduleMessage.TaskType;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ExecutorLogMessage extends BaseMessage {

	private TaskType taskType;

	private String executor;

	private String task;

	private String scheduleId;

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

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
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

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

}
