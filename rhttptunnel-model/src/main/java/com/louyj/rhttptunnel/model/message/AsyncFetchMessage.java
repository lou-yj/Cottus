package com.louyj.rhttptunnel.model.message;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class AsyncFetchMessage extends BaseMessage {

	private int second = 3;

	public AsyncFetchMessage(ClientInfo client) {
		super(client);
	}

	public AsyncFetchMessage(ClientInfo client, int second) {
		super(client);
		this.second = second;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

}
