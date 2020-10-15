package com.louyj.cottus.server.session;

import java.security.Key;
import java.util.Set;

import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.message.consts.NotifyEventType;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class WorkerSession {

	private long startTime = System.currentTimeMillis();

	private long lastTime = System.currentTimeMillis();

	private String workerId;

	private Set<String> clientIds = Sets.newHashSet();

	private String aesKey;

	private Key publicKey;

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}

	public Key getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(Key publicKey) {
		this.publicKey = publicKey;
	}

	public Set<String> getClientIds() {
		return clientIds;
	}

	public WorkerSession(String clientId) {
		super();
		this.workerId = clientId;
	}

	public Set<String> allClientIds() {
		return clientIds;
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

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public void onClientRemove(WorkerSessionManager workerSessionManager, String clientId) throws InterruptedException {
		clientIds.remove(clientId);
		workerSessionManager.getNotifyQueue(this).put(Pair.of(NotifyEventType.CLIENTS_CHANGED, clientIds));
		workerSessionManager.getQueue(this, clientId).close();
	}

	public void destory(WorkerSessionManager workerSessionManager) throws InterruptedException {
		for (String clientId : clientIds) {
			workerSessionManager.getQueue(this, clientId).close();
		}
	}

}
