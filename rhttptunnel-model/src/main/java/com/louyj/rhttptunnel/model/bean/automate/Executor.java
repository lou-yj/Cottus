package com.louyj.rhttptunnel.model.bean.automate;

import java.util.Map;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsCollectType;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsType;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class Executor {

	public static enum ScheduleType {
		CRONJOB
	}

	private String name;

	private ScheduleType type = ScheduleType.CRONJOB;

	private String scheduleExpression;

	private Map<String, String> targets = Maps.newHashMap();

	private String language;

	private String script;

	private String scriptFile;

	private Map<String, String> params = Maps.newHashMap();

	private MetricsCollectType metricsCollectType = MetricsCollectType.EXITVALUE_WRAPPER;

	private MetricsType metricsType = MetricsType.STANDARD;

	private long timeout = 600;

	private boolean collectStdLog = true;

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

	public boolean isCollectStdLog() {
		return collectStdLog;
	}

	public void setCollectStdLog(boolean collectStdLog) {
		this.collectStdLog = collectStdLog;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ScheduleType getType() {
		return type;
	}

	public void setType(ScheduleType type) {
		this.type = type;
	}

	public String getScheduleExpression() {
		return scheduleExpression;
	}

	public void setScheduleExpression(String scheduleExpression) {
		this.scheduleExpression = scheduleExpression;
	}

	public Map<String, String> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, String> targets) {
		this.targets = targets;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
