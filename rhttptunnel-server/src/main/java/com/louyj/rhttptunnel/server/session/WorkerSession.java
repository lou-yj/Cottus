package com.louyj.rhttptunnel.server.session;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.message.BaseMessage;

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

	private String clientId;

	private Map<String, BlockingQueue<BaseMessage>> queues = Maps.newConcurrentMap();

	private BlockingQueue<Set<String>> clientIdQueue = new LinkedBlockingQueue<>();

	public WorkerSession(String clientId) {
		super();
		this.clientId = clientId;
	}

	public Set<String> allClientIds() {
		return Sets.newHashSet(queues.keySet());
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

	public BlockingQueue<BaseMessage> getMessageQueue(String clientId) throws InterruptedException {
		return getQueue(clientId);
	}

	public void putMessage(String clientId, BaseMessage message) throws InterruptedException {
		getQueue(clientId).put(message);
	}

	public BlockingQueue<Set<String>> getClientIdQueue() {
		return clientIdQueue;
	}

	private synchronized BlockingQueue<BaseMessage> getQueue(String clientId) throws InterruptedException {
		BlockingQueue<BaseMessage> messageQueue = queues.get(clientId);
		if (messageQueue == null) {
			messageQueue = new LinkedBlockingDeque<BaseMessage>(50);
			queues.put(clientId, messageQueue);
			clientIdQueue.put(queues.keySet());
		}
		return messageQueue;
	}

	public void onClientRemove(String clientId) throws InterruptedException {
		queues.remove(clientId);
		clientIdQueue.put(queues.keySet());
	}

}
