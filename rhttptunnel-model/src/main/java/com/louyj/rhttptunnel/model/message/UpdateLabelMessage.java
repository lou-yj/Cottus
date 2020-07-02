package com.louyj.rhttptunnel.model.message;

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
public class UpdateLabelMessage extends BaseMessage {

	private Map<String, String> setLabels = Maps.newHashMap();

	private Set<String> delLabels = Sets.newHashSet();

	@JsonCreator
	public UpdateLabelMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public UpdateLabelMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public Map<String, String> getSetLabels() {
		return setLabels;
	}

	public void setSetLabels(Map<String, String> setLabels) {
		this.setLabels = setLabels;
	}

	public Set<String> getDelLabels() {
		return delLabels;
	}

	public void setDelLabels(Set<String> delLabels) {
		this.delLabels = delLabels;
	}

}
