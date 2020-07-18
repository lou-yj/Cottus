package com.louyj.rhttptunnel.server.session;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ClientSession {

	private long startTime = System.currentTimeMillis();

	private long lastTime = System.currentTimeMillis();

	private String clientId;

	private List<String> workerIds;

	private String cwd;

	private Set<String> exchangeIds = Sets.newHashSet();

	public Set<String> getExchangeIds() {
		return exchangeIds;
	}

	public void setExchangeIds(Set<String> exchangeIds) {
		this.exchangeIds = exchangeIds;
	}

	public String getCwd() {
		return cwd;
	}

	public void setCwd(String cwd) {
		this.cwd = cwd;
	}

	public ClientSession(String clientId) {
		super();
		this.clientId = clientId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public List<String> getWorkerIds() {
		return workerIds;
	}

	public void setWorkerIds(List<String> workerIds) {
		this.workerIds = workerIds;
	}

}
