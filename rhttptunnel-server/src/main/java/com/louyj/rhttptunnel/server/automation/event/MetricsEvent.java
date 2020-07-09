package com.louyj.rhttptunnel.server.automation.event;

import java.util.Map;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage;
import com.louyj.rhttptunnel.server.automation.ScheduledTaskAudit;

/**
 *
 * Create at 2020年7月9日
 *
 * @author Louyj
 *
 */
public class MetricsEvent {

	private String scheduleId;

	private String executorName;

	private String taskName;

	private String metricsName;

	private Map<String, Object> params = Maps.newHashMap();

	private Map<String, Object> sre = Maps.newHashMap();

	private Map<String, Object> tags = Maps.newHashMap();

	private Map<String, Object> fields = Maps.newHashMap();

	private Long timestamp;

	public static MetricsEvent make(ScheduledTaskAudit audit, TaskMetricsMessage metricsMessage) {
		MetricsEvent event = new MetricsEvent();
		event.setScheduleId(audit.getScheduleId());
		event.setExecutorName(audit.getExecutor());
		event.setTaskName(audit.getName());
		event.setMetricsName(metricsMessage.getName());
		event.setParams(audit.getParams());
		event.setSre(metricsMessage.getSre());
		event.setTags(metricsMessage.getTags());
		event.setFields(metricsMessage.getFields());
		event.setTimestamp(metricsMessage.getTimestamp());
		return event;
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

	public String getMetricsName() {
		return metricsName;
	}

	public void setMetricsName(String metricsName) {
		this.metricsName = metricsName;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Map<String, Object> getSre() {
		return sre;
	}

	public void setSre(Map<String, Object> sre) {
		this.sre = sre;
	}

	public Map<String, Object> getTags() {
		return tags;
	}

	public void setTags(Map<String, Object> tags) {
		this.tags = tags;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
