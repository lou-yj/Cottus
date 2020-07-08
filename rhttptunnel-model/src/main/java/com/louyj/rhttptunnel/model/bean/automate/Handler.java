package com.louyj.rhttptunnel.model.bean.automate;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class Handler {

	private String alarmName;

	private Map<String, String> targets = Maps.newHashMap();

	private String script;

	private String scriptFile;

	private long timeout;

	public Map<String, String> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, String> targets) {
		this.targets = targets;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getAlarmName() {
		return alarmName;
	}

	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}

}
