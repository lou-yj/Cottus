package com.louyj.rhttptunnel.model.message;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class NoContentMessage extends BaseMessage {

	public NoContentMessage(ClientInfo client) {
		super(client);
	}

	public NoContentMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

}
