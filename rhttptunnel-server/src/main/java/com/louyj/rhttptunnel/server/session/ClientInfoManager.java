package com.louyj.rhttptunnel.server.session;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.ClientInfo;

@Component
public class ClientInfoManager {

	@Autowired
	private Ignite ignite;

	private IgniteCache<Object, Object> clientInfoCache;
	private IgniteAtomicLong indexCounter;

	@PostConstruct
	public void init() {
		clientInfoCache = ignite.getOrCreateCache(new CacheConfiguration<>().setName("clientInfoCache")
				.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.DAYS, 100))));
		indexCounter = ignite.atomicLong("ClientIdIndexCounter", 0, true);
		ClientInfo.SERVER.setUuid("s:0");
		clientInfoCache.put(ClientInfo.SERVER.identify(), ClientInfo.SERVER);
	}

	public ClientInfo findClientInfo(String clientId) {
		ClientInfo clientInfo = (ClientInfo) clientInfoCache.get(clientId);
		return clientInfo;
	}

	public void registryClient(ClientInfo clientInfo) {
		String id = "c:" + indexCounter.incrementAndGet();
		clientInfo.setUuid(id);
		clientInfoCache.put(clientInfo.identify(), clientInfo);
	}

	public void registryWorker(ClientInfo clientInfo) {
		String id = "w:" + indexCounter.incrementAndGet();
		clientInfo.setUuid(id);
		clientInfoCache.put(clientInfo.identify(), clientInfo);
	}

}
