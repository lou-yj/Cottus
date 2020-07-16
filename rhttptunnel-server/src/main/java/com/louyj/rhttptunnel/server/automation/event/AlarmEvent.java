package com.louyj.rhttptunnel.server.automation.event;

import java.util.List;
import java.util.Map;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.bean.automate.AlarmInhibitor;
import com.louyj.rhttptunnel.model.bean.automate.HandlerProcessInfo;

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

	@QuerySqlField
	private List<Pair<String, Map<String, Object>>> tags = Lists.newArrayList();

	@QuerySqlField
	private List<HandlerProcessInfo> handlerInfo = Lists.newArrayList();

	@QuerySqlField
	private String alarmSilencerId;

	@QuerySqlField
	private AlarmInhibitor alarmInhibitor;

	public AlarmInhibitor getAlarmInhibitor() {
		return alarmInhibitor;
	}

	public void setAlarmInhibitor(AlarmInhibitor alarmInhibitor) {
		this.alarmInhibitor = alarmInhibitor;
	}

	public String getAlarmSilencerId() {
		return alarmSilencerId;
	}

	public void setAlarmSilencerId(String alarmSilencerId) {
		this.alarmSilencerId = alarmSilencerId;
	}

	public List<HandlerProcessInfo> getHandlerInfo() {
		return handlerInfo;
	}

	public void setHandlerInfo(List<HandlerProcessInfo> handlerInfo) {
		this.handlerInfo = handlerInfo;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public List<Pair<String, Map<String, Object>>> getTags() {
		return tags;
	}

	public void setTags(List<Pair<String, Map<String, Object>>> tags) {
		this.tags = tags;
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
		for (Pair<String, Map<String, Object>> pair : tags) {
			map.putAll(pair.getRight());
		}
		return map;
	}

}
