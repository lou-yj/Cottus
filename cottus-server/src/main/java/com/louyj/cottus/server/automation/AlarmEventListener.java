package com.louyj.cottus.server.automation;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.espertech.esper.client.EventBean;
import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.cottus.server.automation.event.AlarmEvent;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class AlarmEventListener implements com.espertech.esper.client.UpdateListener {

	private Alarmer alarmer;
	private AlarmService alarmService;

	public AlarmEventListener(Alarmer alarmer, AlarmService handlerService) {
		super();
		this.alarmer = alarmer;
		this.alarmService = handlerService;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		notifyAlarm(newEvents);
	}

	protected void notifyAlarm(EventBean[] newEvents) {
		for (EventBean eventBean : newEvents) {
			AlarmEvent alarmEvent = new AlarmEvent();
			alarmEvent.setAlarmRule(alarmer.getName());
			alarmEvent.setAlarmTime(System.currentTimeMillis());
			String[] propertyNames = eventBean.getEventType().getPropertyNames();
			for (String propertyName : propertyNames) {
				Object value = eventBean.get(propertyName);
				alarmEvent.getFields().put(propertyName, value);
			}
			alarmEvent.setAlarmGroup(makeGroup(alarmEvent.getFields()));
			alarmService.handleAlarm(alarmEvent);
		}
	}

	private String makeGroup(Map<String, Object> alarmEvent) {
		List<String> items = Lists.newArrayList();
		for (String key : alarmer.getGroupKeys()) {
			String value = MapUtils.getString(alarmEvent, key, StringUtils.EMPTY);
			items.add(value);
		}
		return StringUtils.join(items, ":");
	}

}
