package com.louyj.rhttptunnel.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.automate.Alarmer;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ListAlarmersMessage extends BaseMessage {

	private List<Alarmer> alarmers;

	@JsonCreator
	public ListAlarmersMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ListAlarmersMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<Alarmer> getAlarmers() {
		return alarmers;
	}

	public void setAlarmers(List<Alarmer> alarmers) {
		this.alarmers = alarmers;
	}

}
