package com.louyj.rhttptunnel.server.auth;

import java.util.Set;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 *
 * Create at 2020年7月21日
 *
 * @author Louyj
 *
 */
public class Group {

	@QuerySqlField(index = true)
	private String name;

	@QuerySqlField
	private Set<String> groups;

	@QuerySqlField
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
