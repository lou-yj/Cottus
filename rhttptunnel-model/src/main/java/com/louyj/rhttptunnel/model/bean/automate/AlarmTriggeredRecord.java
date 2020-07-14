package com.louyj.rhttptunnel.model.bean.automate;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class AlarmTriggeredRecord {

	private long alarmTime;

	private String alarmGroup;

	private Map<String, Object> fields = Maps.newHashMap();

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
