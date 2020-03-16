package com.louyj.rhttptunnel.model.message;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AsyncExecAckMessage extends BaseMessage {

	public AsyncExecAckMessage(ClientInfo client) {
		super(client);
	}

	public AsyncExecAckMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public static AsyncExecAckMessage cack(ClientInfo client, String exchangeId) {
		return new AsyncExecAckMessage(client, exchangeId);
	}

	public static AsyncExecAckMessage sack(String exchangeId) {
		return new AsyncExecAckMessage(SERVER, exchangeId);
	}

}
