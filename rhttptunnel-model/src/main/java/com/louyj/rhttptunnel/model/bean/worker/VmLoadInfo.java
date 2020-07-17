package com.louyj.rhttptunnel.model.bean.worker;

import java.util.Map;

/**
 *
 * Create at 2020年7月17日
 *
 * @author Louyj
 *
 */
public class VmLoadInfo {

	private String name;

	private String vendor;

	private String version;

	private long uptime;

	private long startTime;

	private MemoryInfo heapMemoryUsage;

	private MemoryInfo nonHeapMemoryUsage;

	private Map<String, String> systemProperties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getUptime() {
		return uptime;
	}

	public void setUptime(long uptime) {
		this.uptime = uptime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public MemoryInfo getHeapMemoryUsage() {
		return heapMemoryUsage;
	}

	public void setHeapMemoryUsage(MemoryInfo heapMemoryUsage) {
		this.heapMemoryUsage = heapMemoryUsage;
	}

	public MemoryInfo getNonHeapMemoryUsage() {
		return nonHeapMemoryUsage;
	}

	public void setNonHeapMemoryUsage(MemoryInfo nonHeapMemoryUsage) {
		this.nonHeapMemoryUsage = nonHeapMemoryUsage;
	}

	public Map<String, String> getSystemProperties() {
		return systemProperties;
	}

	public void setSystemProperties(Map<String, String> systemProperties) {
		this.systemProperties = systemProperties;
	}

}
