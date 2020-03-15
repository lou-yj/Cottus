package com.louyj.rhttptunnel.model.message;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
public class SleepMessage extends BaseMessage {

	private long second = 5;

	public SleepMessage(ClientInfo client, long second) {
		super(client);
		this.second = second;
	}

	public long getSecond() {
		return second;
	}

}
