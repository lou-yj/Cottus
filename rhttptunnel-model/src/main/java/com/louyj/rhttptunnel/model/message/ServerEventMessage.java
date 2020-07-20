package com.louyj.rhttptunnel.model.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.louyj.rhttptunnel.model.message.consts.NotifyEventType;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ServerEventMessage extends BaseMessage implements IWorkerParallelMessage {

	private NotifyEventType type;

	private Object event;

	@JsonCreator
	public ServerEventMessage(@JsonProperty("client") ClientInfo client) {
		super(client);
	}

	public ServerEventMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public NotifyEventType getType() {
		return type;
	}

	public void setType(NotifyEventType type) {
		this.type = type;
	}

	public Object getEvent() {
		return event;
	}

	public void setEvent(Object event) {
		this.event = event;
	}

}
