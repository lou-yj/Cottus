package com.louyj.cottus.model.message.server;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.louyj.cottus.model.message.ClientInfo;
import com.louyj.cottus.model.message.IWorkerParallelMessage;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class TaskScheduleMessage extends ServerMessage implements IWorkerParallelMessage {

	public static enum ScriptContentType {
		TEXT, FILE
	}

	public static enum MetricsCollectType {
		STDOUT_METRICS, STDERR_METRICS, FILE_METRICS, EXITVALUE_WRAPPER
	}

	public static enum MetricsType {
		STANDARD, PROMETHEUS
	}

	public static enum TaskType {
		EXECUTOR, HANDLER
	}

	private TaskType type = TaskType.EXECUTOR;

	private String scheduledId;

	private String executor;

	private String name;

	private String commitId;

	private String language;

	private String script;

	private Map<String, String> labels;

	private Map<String, Object> params;

	private List<Map<String, Object>> correlationParams;

	private Map<String, Object> expected = Maps.newHashMap();

	private ScriptContentType scriptContentType = ScriptContentType.TEXT;

	private MetricsCollectType metricsCollectType = MetricsCollectType.EXITVALUE_WRAPPER;

	private MetricsType metricsType = MetricsType.STANDARD;

	private long timeout = 600;

	private boolean collectStdLog = true;

	public List<Map<String, Object>> getCorrelationParams() {
		return correlationParams;
	}

	public void setCorrelationParams(List<Map<String, Object>> correlationParams) {
		this.correlationParams = correlationParams;
	}

	public TaskType getType() {
		return type;
	}

	public void setType(TaskType type) {
		this.type = type;
	}

	public String getScheduledId() {
		return scheduledId;
	}

	public void setScheduledId(String scheduledId) {
		this.scheduledId = scheduledId;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public Map<String, Object> getExpected() {
		return expected;
	}

	public void setExpected(Map<String, Object> expected) {
		this.expected = expected;
	}

	@JsonCreator
	public TaskScheduleMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public TaskScheduleMessage(ClientInfo client, String exchangeId, String serverMsgId) {
		super(client, exchangeId, serverMsgId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCollectStdLog() {
		return collectStdLog;
	}

	public void setCollectStdLog(boolean collectStdLog) {
		this.collectStdLog = collectStdLog;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public ScriptContentType getScriptContentType() {
		return scriptContentType;
	}

	public void setScriptContentType(ScriptContentType scriptContentType) {
		this.scriptContentType = scriptContentType;
	}

	public MetricsCollectType getMetricsCollectType() {
		return metricsCollectType;
	}

	public void setMetricsCollectType(MetricsCollectType metricsCollectType) {
		this.metricsCollectType = metricsCollectType;
	}

	public MetricsType getMetricsType() {
		return metricsType;
	}

	public void setMetricsType(MetricsType metricsType) {
		this.metricsType = metricsType;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
