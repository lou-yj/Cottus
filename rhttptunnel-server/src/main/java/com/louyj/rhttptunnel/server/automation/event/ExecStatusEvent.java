package com.louyj.rhttptunnel.server.automation.event;

import java.util.Map;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.server.TaskAckMessage;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;
import com.louyj.rhttptunnel.server.automation.ScheduledTaskAudit;

/**
 *
 * Create at 2020年7月9日
 *
 * @author Louyj
 *
 */
public class ExecStatusEvent {

	private String scheduleId;

	private String executorName;

	private String taskName;

	private Map<String, Object> params = Maps.newHashMap();

	private ExecuteStatus status;

	private String message;

	private Long timestamp;

	public static ExecStatusEvent make(ScheduledTaskAudit audit, TaskAckMessage ackMessage) {
		ExecStatusEvent event = new ExecStatusEvent();
		event.setScheduleId(audit.getScheduleId());
		event.setExecutorName(audit.getExecutor());
		event.setTaskName(audit.getName());
		event.setParams(audit.getParams());
		event.setTimestamp(System.currentTimeMillis());
		event.setStatus(ackMessage.getStatus());
		event.setMessage(ackMessage.getMessage());
		return event;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getExecutorName() {
		return executorName;
	}

	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public ExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(ExecuteStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
