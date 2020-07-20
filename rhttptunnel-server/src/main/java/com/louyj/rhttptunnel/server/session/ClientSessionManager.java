package com.louyj.rhttptunnel.server.session;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.consts.NotifyEventType;
import com.louyj.rhttptunnel.server.IgniteRegistry;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class ClientSessionManager {

	static final String CLIENT_CACHE = "clientCache";

	@Value("${client.session.timeout:120}")
	private int clientSessionTimeout = 120;
	@Value("${client.exchange.timeout:3600}")
	private int clientExchangeTimeout = 3600;
	@Autowired
	private IgniteRegistry igniteRegistry;

	private IgniteCache<String, ClientSession> clientCache;
	private IgniteCache<String, String> exchangeCache;

	@PostConstruct
	public void init() {
		clientCache = igniteRegistry.getOrCreateCache(CLIENT_CACHE, clientSessionTimeout, TimeUnit.SECONDS);
		exchangeCache = igniteRegistry.getOrCreateCache("exchangeCache", clientExchangeTimeout, TimeUnit.SECONDS);
	}

	public IgniteQueue<Pair<NotifyEventType, Object>> getNotifyQueue(ClientSession clientSession)
			throws InterruptedException {
		return igniteRegistry.<Pair<NotifyEventType, Object>>queue("clientnotify:" + clientSession.getClientId(), 100);
	}

	public void putMessage(String clientId, BaseMessage message) {
		getQueue(clientId).put(message);
	}

	public BaseMessage pollMessage(String clientId, int time, TimeUnit unit) {
		return getQueue(clientId).poll(time, unit);
	}

	IgniteQueue<BaseMessage> getQueue(String clientId) {
		return igniteRegistry.queue("client:" + clientId, 100);
	}

	public boolean update(String clientId, String exchangeId) {
		ClientSession session = clientCache.get(clientId);
		if (session == null) {
			session = new ClientSession(clientId);
		}
		session.setLastTime(System.currentTimeMillis());
		session.getExchangeIds().add(exchangeId);
		clientCache.put(clientId, session);
		exchangeCache.put(exchangeId, clientId);
		return true;
	}

	public void update(ClientSession clientSession) {
		if (clientSession == null) {
			return;
		}
		clientCache.put(clientSession.getClientId(), clientSession);
	}

	public ClientSession sessionByCid(String clientId) {
		if (clientId == null) {
			return null;
		}
		return clientCache.get(clientId);
	}

	public ClientSession sessionByEid(String exchangeId) {
		String identiry = exchangeCache.get(exchangeId);
		if (identiry == null) {
			return null;
		}
		return clientCache.get(identiry);
	}

	public void clientExit(String identify, ClientSession session) {
		clientCache.remove(identify);
		for (String exchangeId : session.getExchangeIds()) {
			exchangeCache.remove(exchangeId);
		}
	}

}
