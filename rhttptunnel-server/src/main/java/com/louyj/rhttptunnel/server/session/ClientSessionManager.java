package com.louyj.rhttptunnel.server.session;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;

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

	@Autowired
	private Ignite ignite;

	private IgniteCache<String, ClientSession> clientCache;
	private IgniteCache<String, String> exchangeCache;
	private CollectionConfiguration colCfg;

	@PostConstruct
	public void init() {
		clientCache = ignite.getOrCreateCache(new CacheConfiguration<String, ClientSession>().setName(CLIENT_CACHE)
				.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 5))));
		exchangeCache = ignite.getOrCreateCache(new CacheConfiguration<String, String>().setName("exchangeCache")
				.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 1))));
		colCfg = new CollectionConfiguration();
		colCfg.setCollocated(true);
		colCfg.setBackups(1);
	}

	public void putMessage(String clientId, BaseMessage message) {
		getQueue(clientId).put(message);
	}

	public BaseMessage pollMessage(String clientId, int time, TimeUnit unit) {
		return getQueue(clientId).poll(time, unit);
	}

	IgniteQueue<BaseMessage> getQueue(String clientId) {
		return ignite.<BaseMessage>queue("client:" + clientId, 100, colCfg);
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
