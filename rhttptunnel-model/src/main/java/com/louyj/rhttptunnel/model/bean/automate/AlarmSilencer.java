package com.louyj.rhttptunnel.model.bean.automate;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月16日
 *
 * @author Louyj
 *
 */
public class AlarmSilencer {

	private boolean regexMatch = false;

	private Map<String, Object> matched = Maps.newHashMap();

	private Long startTime;

	private Long endTime;

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

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

}
