package com.louyj.rhttptunnel.model.bean.automate;

import java.util.List;

import com.google.common.collect.Lists;

/**
 *
 * Create at 2020年7月15日
 *
 * @author Louyj
 *
 */
public class AlarmTrace {

	private AlarmTriggeredRecord record;

	private List<HandlerProcessInfo> handlerInfos = Lists.newArrayList();

	private AlarmSilencer alarmSilencer;

	private AlarmInhibitor alarmInhibitor;

	public AlarmInhibitor getAlarmInhibitor() {
		return alarmInhibitor;
	}

	public void setAlarmInhibitor(AlarmInhibitor alarmInhibitor) {
		this.alarmInhibitor = alarmInhibitor;
	}

	public AlarmSilencer getAlarmSilencer() {
		return alarmSilencer;
	}

	public void setAlarmSilencer(AlarmSilencer alarmSilencer) {
		this.alarmSilencer = alarmSilencer;
	}

	public AlarmTriggeredRecord getRecord() {
		return record;
	}

	public void setRecord(AlarmTriggeredRecord record) {
		this.record = record;
	}

	public List<HandlerProcessInfo> getHandlerInfos() {
		return handlerInfos;
	}

	public void setHandlerInfos(List<HandlerProcessInfo> handlerInfos) {
		this.handlerInfos = handlerInfos;
	}

}
