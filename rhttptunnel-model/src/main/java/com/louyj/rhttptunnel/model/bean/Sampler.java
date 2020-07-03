package com.louyj.rhttptunnel.model.bean;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class Sampler {

	public static enum SamplerType {
		CRONJOB
	}

	private String name;

	private Map<String, String> targets = Maps.newHashMap();

	private SamplerType type = SamplerType.CRONJOB;

	private String schedule;

	private String script;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, String> targets) {
		this.targets = targets;
	}

	public SamplerType getType() {
		return type;
	}

	public void setType(SamplerType type) {
		this.type = type;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

}
