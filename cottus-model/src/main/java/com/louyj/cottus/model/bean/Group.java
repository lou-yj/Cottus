package com.louyj.cottus.model.bean;

import java.util.Set;

/**
 *
 * Create at 2020年7月21日
 *
 * @author Louyj
 *
 */
public class Group {

	private String name;

	private Set<String> groups;

	private Set<String> commands;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getGroups() {
		return groups;
	}

	public void setGroups(Set<String> groups) {
		this.groups = groups;
	}

	public Set<String> getCommands() {
		return commands;
	}

	public void setCommands(Set<String> commands) {
		this.commands = commands;
	}

}
