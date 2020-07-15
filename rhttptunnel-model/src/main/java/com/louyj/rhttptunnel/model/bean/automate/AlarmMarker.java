package com.louyj.rhttptunnel.model.bean.automate;

import java.io.File;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月15日
 *
 * @author Louyj
 *
 */
public class AlarmMarker {

	private String name;

	private int order = 0;

	private Map<String, Object> matched = Maps.newHashMap();

	private boolean regexMatch = false;

	private Map<String, Object> tags = Maps.newHashMap();

	private Map<String, String> properties = Maps.newHashMap();

	public void check(File ruleFile) {
		if (StringUtils.isBlank(name)) {
			String message = String.format("Name missing of alarm markerin file %s", ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
		if (MapUtils.isEmpty(matched)) {
			String message = String.format("Bad format for alarm marker %s in file %s", name, ruleFile.getName());
			throw new IllegalArgumentException(message);
		}
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Map<String, Object> getMatched() {
		return matched;
	}

	public void setMatched(Map<String, Object> matched) {
		this.matched = matched;
	}

	public boolean isRegexMatch() {
		return regexMatch;
	}

	public void setRegexMatch(boolean regexMatch) {
		this.regexMatch = regexMatch;
	}

	public Map<String, Object> getTags() {
		return tags;
	}

	public void setTags(Map<String, Object> tags) {
		this.tags = tags;
	}

}
