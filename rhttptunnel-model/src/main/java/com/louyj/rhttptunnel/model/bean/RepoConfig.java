package com.louyj.rhttptunnel.model.bean;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class RepoConfig {

	private String url;

	private String branch;

	private String username;

	private String password;

	private String ruleDirectory = "rules";

	private String scriptDirectory = "scripts";

	public String getRuleDirectory() {
		return ruleDirectory;
	}

	public void setRuleDirectory(String ruleDirectory) {
		this.ruleDirectory = ruleDirectory;
	}

	public String getScriptDirectory() {
		return scriptDirectory;
	}

	public void setScriptDirectory(String scriptDirectory) {
		this.scriptDirectory = scriptDirectory;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
