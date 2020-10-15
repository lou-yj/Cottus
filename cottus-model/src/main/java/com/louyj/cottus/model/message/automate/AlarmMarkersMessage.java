package com.louyj.cottus.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.bean.automate.AlarmMarker;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AlarmMarkersMessage extends BaseMessage {

	private List<AlarmMarker> markers;

	@JsonCreator
	public AlarmMarkersMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public AlarmMarkersMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<AlarmMarker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<AlarmMarker> markers) {
		this.markers = markers;
	}

}
