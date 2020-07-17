package com.louyj.rhttptunnel.server.session;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.louyj.rhttptunnel.model.message.BaseMessage;

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

	private BlockingQueue<BaseMessage> messageQueue = new LinkedBlockingDeque<BaseMessage>(100);

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

	public BlockingQueue<BaseMessage> getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(BlockingQueue<BaseMessage> messageQueue) {
		this.messageQueue = messageQueue;
	}

	public void putMessage(BaseMessage message) throws InterruptedException {
		this.messageQueue.put(message);
	}

}
