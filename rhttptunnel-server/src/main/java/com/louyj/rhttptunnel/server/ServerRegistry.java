package com.louyj.rhttptunnel.server;

import javax.annotation.PostConstruct;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.ClientInfo;

@Component
public class ServerRegistry {

	@Autowired
	private Ignite ignite;

	private IgniteAtomicLong serverIndexCounter;
	private IgniteCache<Object, ClientInfo> serverInfoCache;

	@PostConstruct
	public void init() {
		serverInfoCache = ignite.getOrCreateCache("serverInfoCache");
		serverIndexCounter = ignite.atomicLong("serverIndexCounter", 0, true);
		ClientInfo.SERVER.setUuid("s:" + serverIndexCounter.incrementAndGet());
		Object consistentId = ignite.cluster().localNode().consistentId();
		serverInfoCache.put(consistentId, ClientInfo.SERVER);
	}

}
