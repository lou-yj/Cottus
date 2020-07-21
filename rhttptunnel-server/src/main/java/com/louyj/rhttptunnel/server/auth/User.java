package com.louyj.rhttptunnel.server.auth;

import java.util.Set;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.common.collect.Sets;

/**
 *
 * Create at 2020年7月21日
 *
 * @author Louyj
 *
 */
public class User {

	@QuerySqlField(index = true)
	private String name;

	@QuerySqlField
	private String password;

	@QuerySqlField
	private Set<String> groups = Sets.newHashSet();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getGroups() {
		return groups;
	}

	public void setGroups(Set<String> groups) {
		this.groups = groups;
	}

}
