package com.louyj.cottus.model.bean.automate;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class ExecutorLog {

	private String ip;

	private String host;

	private String stdout;

	private String stderr;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

}
