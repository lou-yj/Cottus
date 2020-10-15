package com.louyj.cottus.model.message.automate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.cottus.model.bean.automate.AlarmTrace;
import com.louyj.cottus.model.message.BaseMessage;
import com.louyj.cottus.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AlarmerTraceMessage extends BaseMessage {

	private String uuid;

	private AlarmTrace alarmTrace;

	@JsonCreator
	public AlarmerTraceMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public AlarmerTraceMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public AlarmTrace getAlarmTrace() {
		return alarmTrace;
	}

	public void setAlarmTrace(AlarmTrace alarmTrace) {
		this.alarmTrace = alarmTrace;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
