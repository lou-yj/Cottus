package com.louyj.rhttptunnel.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cluster.ClusterNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.ClientInfo;

@Component
public class ServerRegistry {

	@Autowired
	private IgniteRegistry igniteRegistry;

	private IgniteAtomicLong serverIndexCounter;
	private IgniteCache<Object, ClientInfo> serverInfoCache;

	@PostConstruct
	public void init() throws UnknownHostException {
		InetAddress localHost = InetAddress.getLocalHost();
		String ip = localHost.getHostAddress();
		String hostName = localHost.getHostName();
		ClientInfo.SERVER.setHost(hostName);
		ClientInfo.SERVER.setIp(ip);
		serverInfoCache = igniteRegistry.getOrCreateCache("serverInfoCache");
		serverIndexCounter = igniteRegistry.atomicLong("serverIndexCounter", 0, true);
		ClientInfo.SERVER.setUuid("s:" + serverIndexCounter.incrementAndGet());
		Object consistentId = igniteRegistry.localId();
		serverInfoCache.put(consistentId, ClientInfo.SERVER);
	}

	public List<ClientInfo> servers() {
		Collection<ClusterNode> nodes = igniteRegistry.nodes();
		List<ClientInfo> result = Lists.newArrayList();
		for (ClusterNode node : nodes) {
			Object consistentId = node.consistentId();
			ClientInfo clientInfo = serverInfoCache.get(consistentId);
			result.add(clientInfo);
		}
		return result;
	}

	public String masterId() {
		Object consistentId = igniteRegistry.oldestId();
		ClientInfo clientInfo = serverInfoCache.get(consistentId);
		return clientInfo.identify();
	}
}