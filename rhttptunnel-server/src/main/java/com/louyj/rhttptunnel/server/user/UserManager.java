package com.louyj.rhttptunnel.server.user;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.bean.RoleType;

/**
 *
 * Created on 2020年3月20日
 *
 * @author Louyj
 *
 */
@Component
@ConfigurationProperties("user")
public class UserManager {

	private Map<String, UserInfo> auth = Maps.newHashMap();

	public Map<String, UserInfo> getAuth() {
		return auth;
	}

	public void setAuth(Map<String, UserInfo> auth) {
		this.auth = auth;
	}

	public RoleType verify(String user, String password) {
		UserInfo userInfo = auth.get(user);
		if (userInfo == null) {
			return null;
		}
		if (StringUtils.equals(password, userInfo.getPassword())) {
			return userInfo.getRole();
		}
		return null;
	}

}
