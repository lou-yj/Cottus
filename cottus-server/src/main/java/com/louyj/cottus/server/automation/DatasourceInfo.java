package com.louyj.cottus.server.automation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * Create at 2020年7月15日
 *
 * @author Louyj
 *
 */
@ConfigurationProperties("jdbc")
public class DatasourceInfo {

	private String url;

	private String user;

	private String password;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
