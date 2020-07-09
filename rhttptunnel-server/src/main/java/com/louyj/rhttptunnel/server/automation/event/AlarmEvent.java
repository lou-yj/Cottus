package com.louyj.rhttptunnel.server.automation.event;

import java.util.Map;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月9日
 *
 * @author Louyj
 *
 */
public class AlarmEvent {

	public static final String ALARM_RULE = "alarmRule";
	public static final String ALARM_TIME = "alarmTime";
	public static final String ALARM_GROUP = "alarmGroup";

	@QuerySqlField(index = true)
	private String uuid;

	@QuerySqlField(index = true)
	private String alarmRule;

	@QuerySqlField(index = true)
	private long alarmTime;

	@QuerySqlField(index = true)
	private String alarmGroup;

	@QuerySqlField
	private Map<String, Object> fields = Maps.newHashMap();

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public AlarmEvent() {
		super();
	}

	public String getAlarmRule() {
		return alarmRule;
	}

	public void setAlarmRule(String alarmRule) {
		this.alarmRule = alarmRule;
		fields.put(ALARM_RULE, alarmRule);
	}

	public long getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(long alarmTime) {
		this.alarmTime = alarmTime;
		fields.put(ALARM_TIME, alarmTime);
	}

	public String getAlarmGroup() {
		return alarmGroup;
	}

	public void setAlarmGroup(String alarmGroup) {
		this.alarmGroup = alarmGroup;
		fields.put(ALARM_GROUP, alarmGroup);
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = Maps.newHashMap();
		map.putAll(fields);
		return map;
	}

}
