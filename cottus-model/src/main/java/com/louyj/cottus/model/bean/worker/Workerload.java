package com.louyj.cottus.model.bean.worker;

import java.util.List;

import com.google.common.collect.Lists;

/**
 *
 * Create at 2020年7月17日
 *
 * @author Louyj
 *
 */
public class Workerload {

	private List<String> clientIds = Lists.newArrayList();

	private SystemLoadInfo systemLoadInfo = new SystemLoadInfo();

	private VmLoadInfo vmLoadInfo = new VmLoadInfo();

	public VmLoadInfo getVmLoadInfo() {
		return vmLoadInfo;
	}

	public void setVmLoadInfo(VmLoadInfo vmLoadInfo) {
		this.vmLoadInfo = vmLoadInfo;
	}

	public List<String> getClientIds() {
		return clientIds;
	}

	public void setClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}

	public SystemLoadInfo getSystemLoadInfo() {
		return systemLoadInfo;
	}

	public void setSystemLoadInfo(SystemLoadInfo systemLoadInfo) {
		this.systemLoadInfo = systemLoadInfo;
	}

}
