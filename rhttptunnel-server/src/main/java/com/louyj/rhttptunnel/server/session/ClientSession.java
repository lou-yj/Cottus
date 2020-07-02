package com.louyj.rhttptunnel.server.session;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

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

	private ClientInfo clientInfo;

	private List<ClientInfo> workerInfos;

	private String cwd;

	private BlockingQueue<BaseMessage> messageQueue = new LinkedBlockingDeque<BaseMessage>(100);

	public String getCwd() {
		return cwd;
	}

	public void setCwd(String cwd) {
		this.cwd = cwd;
	}

	public ClientSession(ClientInfo clientInfo) {
		super();
		this.clientInfo = clientInfo;
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

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	public List<ClientInfo> getWorkerInfos() {
		return workerInfos;
	}

	public void setWorkerInfos(List<ClientInfo> workerInfos) {
		this.workerInfos = workerInfos;
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
