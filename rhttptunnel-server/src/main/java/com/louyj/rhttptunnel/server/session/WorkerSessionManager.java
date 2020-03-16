package com.louyj.rhttptunnel.server.session;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

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

	private Cache<String, WorkerSession> clients = CacheBuilder.newBuilder().softValues()
			.expireAfterWrite(10, TimeUnit.MINUTES).build();

	public void update(WorkerSession session) {
		clients.put(session.getClientInfo().identify(), session);
	}

	public void update(ClientInfo client) {
		WorkerSession session = clients.getIfPresent(client.identify());
		if (session == null) {
			session = new WorkerSession(client);
		}
		session.setLastTime(System.currentTimeMillis());
		clients.put(client.identify(), session);
	}

	public Collection<WorkerSession> workers() {
		return clients.asMap().values();
	}

	public WorkerSession session(ClientInfo clientInfo) {
		return clients.getIfPresent(clientInfo.identify());
	}

}
