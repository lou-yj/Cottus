package com.louyj.rhttptunnel.model.bean.automate;

import java.io.File;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月16日
 *
 * @author Louyj
 *
 */
public class AlarmInhibitor {

	private String name;
	private boolean regexMatch = false;
	private Map<String, Object> matched = Maps.newHashMap();
	private Map<String, Object> windowMatched = Maps.newHashMap();
	private int timeWindowSize = 600;

	public void check(File ruleFile, String repoCommitIdPath) {
		if (StringUtils.isBlank(name)) {
			String message = String.format("Param name is blank for inhibitor %s in file %s", name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		if (MapUtils.isEmpty(matched)) {
			String message = String.format("Param matched is empty for inhibitor %s in file %s", name,
					ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		if (MapUtils.isEmpty(matched)) {
			String message = String.format("Param windowMatched is empty for inhibitor %s in file %s", name,
					ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRegexMatch() {
		return regexMatch;
	}

	public void setRegexMatch(boolean regexMatch) {
		this.regexMatch = regexMatch;
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

	public int getTimeWindowSize() {
		return timeWindowSize;
	}

	public void setTimeWindowSize(int timeWindowSize) {
		this.timeWindowSize = timeWindowSize;
	}

}
