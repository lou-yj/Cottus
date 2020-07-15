package com.louyj.rhttptunnel.model.bean.automate;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.Pair;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class AlarmTriggeredRecord {

	private String uuid;

	private long alarmTime;

	private String alarmGroup;

	private Map<String, Object> fields = Maps.newHashMap();

	private List<Pair<String, Map<String, Object>>> tags = Lists.newArrayList();

	public List<Pair<String, Map<String, Object>>> getTags() {
		return tags;
	}

	public void setTags(List<Pair<String, Map<String, Object>>> tags) {
		this.tags = tags;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public long getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(long alarmTime) {
		this.alarmTime = alarmTime;
	}

	public String getAlarmGroup() {
		return alarmGroup;
	}

	public void setAlarmGroup(String alarmGroup) {
		this.alarmGroup = alarmGroup;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

}
