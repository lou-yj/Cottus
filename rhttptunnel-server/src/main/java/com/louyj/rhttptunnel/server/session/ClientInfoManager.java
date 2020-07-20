package com.louyj.rhttptunnel.server.session;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.server.IgniteRegistry;

@Component
public class ClientInfoManager {

	@Autowired
	private IgniteRegistry igniteRegistry;

	private IgniteCache<Object, Object> clientInfoCache;
	private IgniteAtomicLong clientIndexCounter;

	@PostConstruct
	public void init() {
		clientInfoCache = igniteRegistry.getOrCreateCache("clientInfoCache", 100, TimeUnit.DAYS);
		clientIndexCounter = igniteRegistry.atomicLong("clientIndexCounter", 0, true);
	}

	public ClientInfo findClientInfo(String clientId) {
		ClientInfo clientInfo = (ClientInfo) clientInfoCache.get(clientId);
		return clientInfo;
	}

	public void registryClient(ClientInfo clientInfo) {
		String id = "c:" + clientIndexCounter.incrementAndGet();
		clientInfo.setUuid(id);
		clientInfoCache.put(clientInfo.identify(), clientInfo);
	}

	public void registryWorker(ClientInfo clientInfo) {
		String id = "w:" + clientIndexCounter.incrementAndGet();
		clientInfo.setUuid(id);
		clientInfoCache.put(clientInfo.identify(), clientInfo);
	}

}
