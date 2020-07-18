package com.louyj.rhttptunnel.server.session;

import java.util.Set;

import com.google.common.collect.Sets;

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
		workerSessionManager.getNotifyQueue(this).put(clientIds);
		workerSessionManager.getQueue(this, clientId).close();
	}

	public void destory(WorkerSessionManager workerSessionManager) throws InterruptedException {
		for (String clientId : clientIds) {
			workerSessionManager.getQueue(this, clientId).close();
		}
	}

}
