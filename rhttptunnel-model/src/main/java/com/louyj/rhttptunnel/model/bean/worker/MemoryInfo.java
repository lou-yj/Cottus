package com.louyj.rhttptunnel.model.bean.worker;

import java.lang.management.MemoryUsage;

/**
 *
 * Create at 2020年7月17日
 *
 * @author Louyj
 *
 */
public class MemoryInfo {

	private long init;
	private long used;
	private long committed;
	private long max;

	public long getInit() {
		return init;
	}

	public long getUsed() {
		return used;
	}

	public long getCommitted() {
		return committed;
	}

	public long getMax() {
		return max;
	}

	public static MemoryInfo of(MemoryUsage mu) {
		MemoryInfo mi = new MemoryInfo();
		mi.init = mu.getInit();
		mi.used = mu.getUsed();
		mi.committed = mu.getCommitted();
		mi.max = mu.getMax();
		return mi;
	}

	public void setInit(long init) {
		this.init = init;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public void setCommitted(long committed) {
		this.committed = committed;
	}

	public void setMax(long max) {
		this.max = max;
	}

}
