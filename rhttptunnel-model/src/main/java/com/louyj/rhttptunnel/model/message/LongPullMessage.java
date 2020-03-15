package com.louyj.rhttptunnel.model.message;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class LongPullMessage extends BaseMessage {

	private int second = 10;

	public LongPullMessage(ClientInfo client) {
		super(client);
	}

	public LongPullMessage(ClientInfo client, int second) {
		super(client);
		this.second = second;
	}

	public int getSecond() {
		return second;
	}

}
