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
public class ClientSessionManager {

	private Cache<String, ClientSession> clients = CacheBuilder.newBuilder().softValues()
			.expireAfterWrite(10, TimeUnit.HOURS).build();

	private Cache<String, String> exchanges = CacheBuilder.newBuilder().softValues()
			.expireAfterWrite(10, TimeUnit.HOURS).build();

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

}
