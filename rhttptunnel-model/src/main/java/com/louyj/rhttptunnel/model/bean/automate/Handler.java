package com.louyj.rhttptunnel.model.bean.automate;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class Handler {

	private String name;
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

	private int order = 0;
	private List<String> preventHandlers = Lists.newArrayList();

	public void check(File ruleFile, String repoCommitIdPath) {
		if (StringUtils.isBlank(language)) {
			String message = String.format("Lanaguage params is blank for handler in file %s", ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<String> getPreventHandlers() {
		return preventHandlers;
	}

	public void setPreventHandlers(List<String> preventHandlers) {
		this.preventHandlers = preventHandlers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
