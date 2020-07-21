package com.louyj.rhttptunnel.model.bean;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 *
 * Create at 2020年7月21日
 *
 * @author Louyj
 *
 */
public class Permission {

	private Set<String> commands = Sets.newHashSet();

	public Set<String> getCommands() {
		return commands;
	}

	public void setCommands(Set<String> commands) {
		this.commands = commands;
	}

}
