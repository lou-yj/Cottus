package com.louyj.rhttptunnel.model.bean.automate;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.automate.Executor.TaskDispatchMode;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsCollectType;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsType;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class ExecutorTask {

	private String name;

	private Map<String, String> targets = Maps.newHashMap();

	private Map<String, Object> expected = Maps.newHashMap();

	private String language;

	private String script;

	private String scriptFile;

	private List<Map<String, Object>> params = Lists.newArrayList();

	private MetricsCollectType metricsCollectType = MetricsCollectType.EXITVALUE_WRAPPER;

	private MetricsType metricsType = MetricsType.STANDARD;

	private TaskDispatchMode taskDispatchMode = TaskDispatchMode.ALL_TARGET;

	private long timeout = 600L;

	private boolean collectStdLog = true;

	private int maxRetry = 0;

	private int retryBackoff = 1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void check(String name, File ruleFile, String repoCommitIdPath) {
		if (StringUtils.isAnyBlank(language)) {
			String message = String.format("Bad format for executor %s in file %s", name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		if (StringUtils.isAllBlank(script, scriptFile)) {
			String message = String.format("Both script and scriptFile params are blank for executor %s in file %s",
					name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		if (MapUtils.isEmpty(targets)) {
			String message = String.format("Target is empty for executor %s in file %s", name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
	}

	public ExecutorTask clone() {
		try {
			ObjectMapper jackson = JsonUtils.jackson();
			String json = jackson.writeValueAsString(this);
			return jackson.readValue(json, this.getClass());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public TaskDispatchMode getTaskDispatchMode() {
		return taskDispatchMode;
	}

	public void setTaskDispatchMode(TaskDispatchMode taskDispatchMode) {
		this.taskDispatchMode = taskDispatchMode;
	}

	public Map<String, String> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, String> targets) {
		this.targets = targets;
	}

	public Map<String, Object> getExpected() {
		return expected;
	}

	public void setExpected(Map<String, Object> expected) {
		this.expected = expected;
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

	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	public List<Map<String, Object>> getParams() {
		return params;
	}

	public void setParams(List<Map<String, Object>> params) {
		this.params = params;
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

	public boolean isCollectStdLog() {
		return collectStdLog;
	}

	public void setCollectStdLog(boolean collectStdLog) {
		this.collectStdLog = collectStdLog;
	}

	public int getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public int getRetryBackoff() {
		return retryBackoff;
	}

	public void setRetryBackoff(int retryBackoff) {
		this.retryBackoff = retryBackoff;
	}

}
