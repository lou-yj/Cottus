package com.louyj.rhttptunnel.server.session;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class WorkerSessionManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Cache<String, WorkerSession> workers = CacheBuilder.newBuilder().softValues()
			.expireAfterWrite(1, TimeUnit.MINUTES).build();

	public void update(WorkerSession session) {
		workers.put(session.getClientInfo().identify(), session);
	}

	public void update(ClientInfo client) {
		WorkerSession session = workers.getIfPresent(client.identify());
		if (session == null) {
			session = new WorkerSession(client);
		}
		session.setLastTime(System.currentTimeMillis());
		workers.put(client.identify(), session);
	}

	public Collection<WorkerSession> workers() {
		return workers.asMap().values();
	}

	public WorkerSession session(ClientInfo clientInfo) {
		if (clientInfo == null) {
			return null;
		}
		return workers.getIfPresent(clientInfo.identify());
	}

	public void remove(ClientInfo clientInfo) {
		workers.invalidate(clientInfo.identify());
	}

	public void onClientRemove(String clientId) {
		for (String workerId : workers.asMap().keySet()) {
			WorkerSession workerSession = workers.getIfPresent(workerId);
			if (workerSession == null) {
				continue;
			}
			try {
				workerSession.onClientRemove(clientId);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}

}
