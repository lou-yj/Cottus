package com.louyj.rhttptunnel.model.message;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class AckMessage extends BaseMessage {

	public AckMessage(ClientInfo client) {
		super(client);
	}

	public AckMessage(ClientInfo client, String exchangeId) {
		super(client);
		setExchangeId(exchangeId);
	}

	public static AckMessage cack(ClientInfo client, String exchangeId) {
		return new AckMessage(client, exchangeId);
	}

	public static AckMessage sack(String exchangeId) {
		return new AckMessage(SERVER, exchangeId);
	}

}
