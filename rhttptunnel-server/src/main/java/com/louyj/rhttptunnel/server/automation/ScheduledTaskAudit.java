package com.louyj.rhttptunnel.server.automation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class ScheduledTaskAudit {

	private String scheduleId;

	private String executor;

	private String name;

	private long time;

	private Map<String, Object> params;

	private Map<String, String> sre;

	private ExecuteStatus status;

	private List<String> metrics = Lists.newArrayList();

	private String stdout;

	private String stderr;

	private String message;

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Map<String, String> getSre() {
		return sre;
	}

	public void setSre(Map<String, String> sre) {
		this.sre = sre;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public List<String> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<String> metrics) {
		this.metrics = metrics;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public ExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(ExecuteStatus status) {
		this.status = status;
	}

}
