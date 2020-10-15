package com.louyj.cottus.server.workerlabel;

import java.io.Serializable;
import java.util.Map;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.common.collect.Maps;

public class LabelRule implements Serializable {

	private static final long serialVersionUID = 1L;

	@QuerySqlField(index = true)
	private HostInfo hostInfo;

	@QuerySqlField
	private Map<String, String> labels = Maps.newHashMap();

	public HostInfo getHostInfo() {
		return hostInfo;
	}

	public void setHostInfo(HostInfo hostInfo) {
		this.hostInfo = hostInfo;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

}
