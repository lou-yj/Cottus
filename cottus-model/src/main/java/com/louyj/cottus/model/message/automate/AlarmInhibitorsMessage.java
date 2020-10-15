package com.louyj.cottus.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.bean.automate.AlarmInhibitor;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AlarmInhibitorsMessage extends BaseMessage {

	private List<AlarmInhibitor> inhibitors;

	@JsonCreator
	public AlarmInhibitorsMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public AlarmInhibitorsMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<AlarmInhibitor> getInhibitors() {
		return inhibitors;
	}

	public void setInhibitors(List<AlarmInhibitor> inhibitors) {
		this.inhibitors = inhibitors;
	}

}
