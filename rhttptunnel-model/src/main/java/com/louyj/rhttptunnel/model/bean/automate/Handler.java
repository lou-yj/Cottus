package com.louyj.rhttptunnel.model.bean.automate;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class Handler {

	private String uuid = UUID.randomUUID().toString();
	private boolean regexMatch = false;
	private Map<String, Object> matched = Maps.newHashMap();
	private Map<String, Object> windowMatched = Maps.newHashMap();

	private int timeWindowSize = 600;
	private int actionWaitCount = 0;
	private int actionAggrTime = 60;

	private String language;
	private String script;
	private String scriptFile;
	private Map<String, String> targets = Maps.newHashMap();
	private Map<String, Object> params = Maps.newHashMap();

	private long timeout = 600;

	public void check(File ruleFile, String repoCommitIdPath) {
		if (StringUtils.isAllBlank(script, scriptFile)) {
			String message = String.format("Both script and scriptFile params are blank for handler in file %s",
					ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		if (MapUtils.isEmpty(matched)) {
			String message = String.format("Target is empty for handler in file %s", ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Map<String, String> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, String> targets) {
		this.targets = targets;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean isRegexMatch() {
		return regexMatch;
	}

	public void setRegexMatch(boolean regexMatch) {
		this.regexMatch = regexMatch;
	}

	public int getTimeWindowSize() {
		return timeWindowSize;
	}

	public void setTimeWindowSize(int timeWindowSize) {
		this.timeWindowSize = timeWindowSize;
	}

	public int getActionWaitCount() {
		return actionWaitCount;
	}

	public void setActionWaitCount(int actionWaitCount) {
		this.actionWaitCount = actionWaitCount;
	}

	public int getActionAggrTime() {
		return actionAggrTime;
	}

	public void setActionAggrTime(int actionAggrTime) {
		this.actionAggrTime = actionAggrTime;
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

	public Map<String, Object> getMatched() {
		return matched;
	}

	public void setMatched(Map<String, Object> matched) {
		this.matched = matched;
	}

	public Map<String, Object> getWindowMatched() {
		return windowMatched;
	}

	public void setWindowMatched(Map<String, Object> windowMatched) {
		this.windowMatched = windowMatched;
	}

}
