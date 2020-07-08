package com.louyj.rhttptunnel.model.message.server;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class TaskScheduleMessage extends ServerMessage {

	public static enum ScriptContentType {
		TEXT, FILE
	}

	public static enum MetricsCollectType {
		STDOUT_METRICS, STDERR_METRICS, FILE_METRICS, EXITVALUE_WRAPPER
	}

	public static enum MetricsType {
		STANDARD, PROMETHEUS
	}

	private String name;

	private String commitId;

	private String language;

	private String script;

	private Map<String, String> labels;

	private Map<String, String> params;

	private ScriptContentType scriptContentType = ScriptContentType.TEXT;

	private MetricsCollectType metricsCollectType = MetricsCollectType.EXITVALUE_WRAPPER;

	private MetricsType metricsType = MetricsType.STANDARD;

	private long timeout = 600;

	private boolean collectStdLog = true;

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

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
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
