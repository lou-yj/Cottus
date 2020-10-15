package com.louyj.cottus.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.bean.automate.AlarmSilencer;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AlarmSilencersMessage extends BaseMessage {

	private List<AlarmSilencer> silencers;

	@JsonCreator
	public AlarmSilencersMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public AlarmSilencersMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<AlarmSilencer> getSilencers() {
		return silencers;
	}

	public void setSilencers(List<AlarmSilencer> silencers) {
		this.silencers = silencers;
	}

}
