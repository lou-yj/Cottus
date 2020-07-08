package com.louyj.rhttptunnel.server.automation;

import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class AlarmEventListener implements com.espertech.esper.client.UpdateListener {

	public static final String ALARM_RULE_NAME = "ruleName";

	private String name;

	public AlarmEventListener(String name) {
		super();
		this.name = name;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {

	}

	protected List<Map<String, Object>> makeOutput(EventBean[] newEvents) {
		List<Map<String, Object>> result = Lists.newArrayList();
		for (EventBean eventBean : newEvents) {
			Map<String, Object> item = Maps.newHashMap();
			String[] propertyNames = eventBean.getEventType().getPropertyNames();
			for (String propertyName : propertyNames) {
				Object value = eventBean.get(propertyName);
				item.put(propertyName, value);
			}
			item.put(ALARM_RULE_NAME, name);
			result.add(item);
		}
		return result;
	}

}
