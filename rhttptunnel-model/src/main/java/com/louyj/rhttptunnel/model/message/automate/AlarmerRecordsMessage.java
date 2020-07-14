package com.louyj.rhttptunnel.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.automate.AlarmTriggeredRecord;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AlarmerRecordsMessage extends BaseMessage {

	private List<AlarmTriggeredRecord> records;

	@JsonCreator
	public AlarmerRecordsMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public AlarmerRecordsMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<AlarmTriggeredRecord> getRecords() {
		return records;
	}

	public void setRecords(List<AlarmTriggeredRecord> records) {
		this.records = records;
	}

}
