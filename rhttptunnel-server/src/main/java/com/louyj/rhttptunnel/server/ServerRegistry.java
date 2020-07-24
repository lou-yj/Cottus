package com.louyj.rhttptunnel.server;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.rhttptunnel.model.util.RsaUtils.formatKey;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
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

	private static final String SUPER_PRIVATE_KEY = "rsa:key:private:super";
	private static final String SUPER_PUBLIC_KEY = "rsa:key:public:super";

	@Autowired
	private IgniteRegistry igniteRegistry;

	@Value("${server.url}")
	private String serverUrl;
	@Value("${data.dir:/data}")
	private String dataDir;

	private IgniteAtomicLong serverIndexCounter;
	private IgniteCache<Object, ServerInfo> serverInfoCache;
	private IgniteCache<Object, Key> rsaKeyCache;
	private Key privateKey;
	private Key publicKey;

	private Key superPrivateKey;
	private Key superPublicKey;

	@PostConstruct
	public void init() throws NoSuchAlgorithmException, IOException {
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

		superPrivateKey = rsaKeyCache.get(SUPER_PRIVATE_KEY);
		superPublicKey = rsaKeyCache.get(SUPER_PUBLIC_KEY);
		if (superPrivateKey == null || superPublicKey == null) {
			Pair<Key, Key> keyPair = RsaUtils.genKeyPair();
			superPrivateKey = keyPair.getLeft();
			superPublicKey = keyPair.getRight();
			rsaKeyCache.put(SUPER_PRIVATE_KEY, superPrivateKey);
			rsaKeyCache.put(SUPER_PUBLIC_KEY, superPublicKey);
		}
		File superPrivateKeyFile = new File(dataDir, "." + RandomStringUtils.random(10, true, true));
		FileUtils.writeStringToFile(superPrivateKeyFile, formatKey(superPrivateKey), UTF_8);
	}

	public Key getPrivateKey() {
		return privateKey;
	}

	public Key getPublicKey() {
		return publicKey;
	}

	public Key getSuperPrivateKey() {
		return superPrivateKey;
	}

	public Key getSuperPublicKey() {
		return superPublicKey;
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
