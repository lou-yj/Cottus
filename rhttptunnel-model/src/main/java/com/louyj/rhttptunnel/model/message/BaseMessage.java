package com.louyj.rhttptunnel.model.message;

import java.util.UUID;

import com.google.gson.Gson;

/**
 * 
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public abstract class BaseMessage {

	private String exchangeId = UUID.randomUUID().toString();
	private long time = System.currentTimeMillis();
	private ClientInfo client;

	public BaseMessage(ClientInfo client) {
		super();
		this.client = client;
	}

	public long getTime() {
		return time;
	}

	public ClientInfo getClient() {
		return client;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

	@Override
	public String toString() {
		return "[" + this.getClass().getSimpleName() + "]" + new Gson().toJson(this);
	}

}
