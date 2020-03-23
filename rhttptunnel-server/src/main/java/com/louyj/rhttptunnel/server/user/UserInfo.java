package com.louyj.rhttptunnel.server.user;

import com.louyj.rhttptunnel.model.bean.RoleType;

/**
 *
 * Created on 2020年3月20日
 *
 * @author Louyj
 *
 */
public class UserInfo {

	private String password;

	private RoleType role;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RoleType getRole() {
		return role;
	}

	public void setRole(RoleType role) {
		this.role = role;
	}

}
