package com.louyj.cottus.model.message;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class DiscoverMessage extends BaseMessage {

	private Map<String, String> labels = Maps.newHashMap();

	private Set<String> noLables = Sets.newHashSet();

	@JsonCreator
	public DiscoverMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public DiscoverMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	public Set<String> getNoLables() {
		return noLables;
	}

	public void setNoLables(Set<String> noLables) {
		this.noLables = noLables;
	}

}
