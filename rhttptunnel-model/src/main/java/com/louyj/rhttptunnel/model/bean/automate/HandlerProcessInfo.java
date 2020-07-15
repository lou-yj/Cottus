package com.louyj.rhttptunnel.model.bean.automate;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage.ExecuteStatus;

/**
 *
 * Create at 2020年7月15日
 *
 * @author Louyj
 *
 */
public class HandlerProcessInfo {

	public static class HandlerExecuteInfo {

		private String host;

		private String ip;

		private Map<String, String> sre = Maps.newHashMap();

		private List<String> metrics = Lists.newArrayList();

		private ExecuteStatus status;

		private String message;

		private String stdout;

		private String stderr;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public Map<String, String> getSre() {
			return sre;
		}

		public void setSre(Map<String, String> sre) {
			this.sre = sre;
		}

		public List<String> getMetrics() {
			return metrics;
		}

		public void setMetrics(List<String> metrics) {
			this.metrics = metrics;
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

	}

	private String alarmId;

	private String handlerId;

	private Long evaluateTime;

	private Pair<String, String> preventedBy;

	private Long scheduledTime;

	private Map<String, Object> params = Maps.newHashMap();

	private List<ClientInfo> targetHosts;

	private String scheduleId;

	private ExecuteStatus status;

	private String message;

	private List<Map<String, Object>> correlationAlarms = Lists.newArrayList();

	private List<HandlerExecuteInfo> executeInfos = Lists.newArrayList();

	public List<HandlerExecuteInfo> getExecuteInfos() {
		return executeInfos;
	}

	public void setExecuteInfos(List<HandlerExecuteInfo> executeInfos) {
		this.executeInfos = executeInfos;
	}

	public String getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(String alarmId) {
		this.alarmId = alarmId;
	}

	public String getHandlerId() {
		return handlerId;
	}

	public void setHandlerId(String handlerId) {
		this.handlerId = handlerId;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public List<Map<String, Object>> getCorrelationAlarms() {
		return correlationAlarms;
	}

	public void setCorrelationAlarms(List<Map<String, Object>> correlationAlarms) {
		this.correlationAlarms = correlationAlarms;
	}

	public Long getEvaluateTime() {
		return evaluateTime;
	}

	public void setEvaluateTime(Long evaluateTime) {
		this.evaluateTime = evaluateTime;
	}

	public Pair<String, String> getPreventedBy() {
		return preventedBy;
	}

	public void setPreventedBy(Pair<String, String> preventedBy) {
		this.preventedBy = preventedBy;
	}

	public Long getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Long scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public List<ClientInfo> getTargetHosts() {
		return targetHosts;
	}

	public void setTargetHosts(List<ClientInfo> targetHosts) {
		this.targetHosts = targetHosts;
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
