package com.louyj.cottus.model.message;

import java.util.List;

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

	private String exchangeId;
	private String clientId;
	private List<String> toWorkers;

	public List<String> getToWorkers() {
		return toWorkers;
	}

	public void setToWorkers(List<String> toWorkers) {
		this.toWorkers = toWorkers;
	}

	public BaseMessage(ClientInfo client) {
		super();
		if (client != null) {
			this.clientId = client.getUuid();
			this.exchangeId = client.nextExchangeId();
		}
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
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
