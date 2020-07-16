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

	private List<AlarmMarker> markers;

	private List<AlarmInhibitor> inhibitors;

	private List<Handler> handlers;

	public List<AlarmInhibitor> getInhibitors() {
		return inhibitors;
	}

	public void setInhibitors(List<AlarmInhibitor> inhibitors) {
		this.inhibitors = inhibitors;
	}

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

	public List<AlarmMarker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<AlarmMarker> markers) {
		this.markers = markers;
	}

}
