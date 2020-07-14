package com.louyj.rhttptunnel.model.message.automate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.bean.automate.ExecutorTaskRecord;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ExecutorRecordsMessage extends BaseMessage {

	private List<ExecutorTaskRecord> records;

	@JsonCreator
	public ExecutorRecordsMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ExecutorRecordsMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public List<ExecutorTaskRecord> getRecords() {
		return records;
	}

	public void setRecords(List<ExecutorTaskRecord> records) {
		this.records = records;
	}

}
