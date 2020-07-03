package com.louyj.rhttptunnel.server.automation;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class SamplerLog {

	public static enum JobStatus {
		SCHEDULED, FINISHED
	}

	private String name;

	private long time;

	private String host;

	private String ip;

	private JobStatus status;

	private String message;

	private long duration;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

}
