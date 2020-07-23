package com.louyj.rhttptunnel.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
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
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.bean.ServerInfo;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.util.RsaUtils;

@Component
public class ServerRegistry {

	private static final String PRIVATE_KEY = "rsa:key:private";
	private static final String PUBLIC_KEY = "rsa:key:public";

	@Autowired
	private IgniteRegistry igniteRegistry;

	@Value("${server.url}")
	private String serverUrl;

	private IgniteAtomicLong serverIndexCounter;
	private IgniteCache<Object, ServerInfo> serverInfoCache;
	private IgniteCache<Object, Key> rsaKeyCache;
	private Key privateKey;
	private Key publicKey;

	@PostConstruct
	public void init() throws UnknownHostException, NoSuchAlgorithmException {
		InetAddress localHost = InetAddress.getLocalHost();
		String ip = localHost.getHostAddress();
		String hostName = localHost.getHostName();
		ClientInfo.SERVER.setHost(hostName);
		ClientInfo.SERVER.setIp(ip);
		serverInfoCache = igniteRegistry.getOrCreateCache("serverInfoCache");
		rsaKeyCache = igniteRegistry.getOrCreateCache("rsaKeyCache");
		serverIndexCounter = igniteRegistry.atomicLong("serverIndexCounter", 0, true);
		ClientInfo.SERVER.setUuid("s:" + serverIndexCounter.incrementAndGet());
		Object consistentId = igniteRegistry.localId();

		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setClientInfo(ClientInfo.SERVER);
		serverInfo.setUrl(serverUrl);
		serverInfoCache.put(consistentId, serverInfo);

		privateKey = rsaKeyCache.get(PRIVATE_KEY);
		publicKey = rsaKeyCache.get(PUBLIC_KEY);
		if (privateKey == null || publicKey == null) {
			Pair<Key, Key> keyPair = RsaUtils.genKeyPair();
			privateKey = keyPair.getLeft();
			publicKey = keyPair.getRight();
			rsaKeyCache.put(PRIVATE_KEY, privateKey);
			rsaKeyCache.put(PUBLIC_KEY, publicKey);
		}
	}

	public Key getPrivateKey() {
		return privateKey;
	}

	public Key getPublicKey() {
		return publicKey;
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
