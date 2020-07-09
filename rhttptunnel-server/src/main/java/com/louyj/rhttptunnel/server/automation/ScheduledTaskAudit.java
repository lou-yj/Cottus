package com.louyj.rhttptunnel.server.automation;

import java.util.List;
import java.util.Map;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

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

	@QuerySqlField(index = true)
	private String scheduleId;

	@QuerySqlField(index = true)
	private String executor;

	@QuerySqlField(index = true)
	private String name;

	@QuerySqlField
	private long time;

	@QuerySqlField
	private Map<String, Object> params;

	@QuerySqlField
	private Map<String, String> sre;

	@QuerySqlField
	private ExecuteStatus status;

	@QuerySqlField
	private List<String> metrics = Lists.newArrayList();

	@QuerySqlField
	private String stdout;

	@QuerySqlField
	private String stderr;

	@QuerySqlField
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
