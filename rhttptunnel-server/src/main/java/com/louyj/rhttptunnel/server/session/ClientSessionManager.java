package com.louyj.rhttptunnel.server.session;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableSet;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class ClientSessionManager implements RemovalListener<String, ClientSession> {

	@Autowired
	private WorkerSessionManager workerSessionManager;

	private Cache<String, ClientSession> clients = CacheBuilder.newBuilder().softValues()
			.expireAfterWrite(1, TimeUnit.MINUTES).removalListener(this).build();

	private Cache<String, String> exchanges = CacheBuilder.newBuilder().softValues().expireAfterWrite(1, TimeUnit.HOURS)
			.build();

	public void update(ClientInfo client, String exchangeId) {
		ClientSession session = clients.getIfPresent(client.identify());
		if (session == null) {
			session = new ClientSession(client);
		}
		session.setLastTime(System.currentTimeMillis());
		clients.put(client.identify(), session);
		exchanges.put(exchangeId, client.identify());
	}

	public Collection<ClientSession> workers() {
		return clients.asMap().values();
	}

	public ClientSession session(ClientInfo clientInfo) {
		if (clientInfo == null) {
			return null;
		}
		return clients.getIfPresent(clientInfo.identify());
	}

	public ClientSession session(String exchangeId) {
		String identiry = exchanges.getIfPresent(exchangeId);
		if (identiry == null) {
			return null;
		}
		return clients.getIfPresent(identiry);
	}

	public void clientExit(String identify) {
		workerSessionManager.onClientRemove(identify);
		for (String exchangeId : ImmutableSet.copyOf(exchanges.asMap().keySet())) {
			String clientId = exchanges.getIfPresent(exchangeId);
			if (StringUtils.equals(clientId, identify)) {
				exchanges.invalidate(exchangeId);
			}
		}
	}

	@Override
	public void onRemoval(RemovalNotification<String, ClientSession> notification) {
		RemovalCause removalCause = notification.getCause();
		if (RemovalCause.EXPIRED.equals(removalCause)) {
			clientExit(notification.getKey());
		}
	}

}
