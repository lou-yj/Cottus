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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.ServerInfo;
import com.louyj.rhttptunnel.model.message.ClientInfo;

@Component
public class ServerRegistry {

	@Autowired
	private IgniteRegistry igniteRegistry;

	@Value("${server.url}")
	private String serverUrl;

	private IgniteAtomicLong serverIndexCounter;
	private IgniteCache<Object, ServerInfo> serverInfoCache;

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

		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setClientInfo(ClientInfo.SERVER);
		serverInfo.setUrl(serverUrl);
		serverInfoCache.put(consistentId, serverInfo);
	}

	public List<ServerInfo> servers() {
		Collection<ClusterNode> nodes = igniteRegistry.nodes();
		List<ServerInfo> result = Lists.newArrayList();
		for (ClusterNode node : nodes) {
			Object consistentId = node.consistentId();
			ServerInfo serverInfo = serverInfoCache.get(consistentId);
			result.add(serverInfo);
		}
		return result;
	}

	public List<String> serverUrls() {
		List<String> serverUrls = Lists.newArrayList();
		servers().forEach(e -> serverUrls.add(e.getUrl()));
		return serverUrls;
	}

	public String masterId() {
		Object consistentId = igniteRegistry.oldestId();
		ServerInfo serverInfo = serverInfoCache.get(consistentId);
		return serverInfo.getClientInfo().identify();
	}
}
