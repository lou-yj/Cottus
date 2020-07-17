package com.louyj.rhttptunnel.model.bean.worker;

/**
 *
 * Create at 2020年7月17日
 *
 * @author Louyj
 *
 */
public class SystemLoadInfo {

	private String name;

	private String version;

	private String arch;

	private int availableProcessors;

	private double loadAverage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public double getLoadAverage() {
		return loadAverage;
	}

	public void setLoadAverage(double loadAverage) {
		this.loadAverage = loadAverage;
	}

}
