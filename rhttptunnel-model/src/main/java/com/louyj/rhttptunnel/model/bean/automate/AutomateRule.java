package com.louyj.rhttptunnel.model.bean.automate;

import java.util.List;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class AutomateRule {

	private Executor executor;

	private List<Alarmer> alarmers;

	private List<Handler> handlers;

	private List<AlarmMarker> alarmMarkers;

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public List<Alarmer> getAlarmers() {
		return alarmers;
	}

	public void setAlarmers(List<Alarmer> alarmers) {
		this.alarmers = alarmers;
	}

	public List<Handler> getHandlers() {
		return handlers;
	}

	public void setHandlers(List<Handler> handlers) {
		this.handlers = handlers;
	}

	public List<AlarmMarker> getAlarmMarkers() {
		return alarmMarkers;
	}

	public void setAlarmMarkers(List<AlarmMarker> alarmMarkers) {
		this.alarmMarkers = alarmMarkers;
	}

}
