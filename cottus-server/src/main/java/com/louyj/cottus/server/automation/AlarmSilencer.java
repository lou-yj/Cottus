package com.louyj.cottus.server.automation;

import java.util.Map;
import java.util.UUID;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月16日
 *
 * @author Louyj
 *
 */
public class AlarmSilencer {

	@QuerySqlField(index = true)
	private String uuid = UUID.randomUUID().toString();

	@QuerySqlField
	private boolean regexMatch = false;

	@QuerySqlField
	private Map<String, Object> matched = Maps.newHashMap();

	@QuerySqlField(index = true)
	private Long startTime;

	@QuerySqlField(index = true)
	private Long endTime;

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
