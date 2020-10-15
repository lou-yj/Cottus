package com.louyj.cottus.model.bean;

import java.util.Map;

import com.google.common.collect.Maps;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Create at 2020年7月1日
 *
 * @author Louyj
 *
 */
public class WorkerInfo {

	private ClientInfo clientInfo;

	private Map<String, String> labels = Maps.newHashMap();

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

}
