package com.louyj.cottus.server.session;

import static com.louyj.rhttptunnel.model.message.consts.NotifyEventType.SERVERS_CHANGED;
import static com.louyj.rhttptunnel.model.message.consts.NotifyEventType.WORKER_LOST;

import java.util.List;

import javax.annotation.PostConstruct;

import com.louyj.cottus.server.IgniteRegistry;
import com.louyj.cottus.server.ServerRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.Event;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgnitePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.message.ClientInfo;

@Component
public class IgniteEventListener implements IgnitePredicate<Event> {

	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IgniteRegistry igniteRegistry;
	@Autowired
	private ServerRegistry serverRegistry;
	@Autowired
	private ClientSessionManager clientSessionManager;
	@Autowired
	private WorkerSessionManager workerSessionManager;
	@Autowired
	private ClientInfoManager clientInfoManager;

	@PostConstruct
	public void init() {
		igniteRegistry.localListen(this, EventType.EVT_CACHE_OBJECT_REMOVED, EventType.EVT_NODE_JOINED,
				EventType.EVT_NODE_LEFT, EventType.EVT_NODE_FAILED);
	}

	@Override
	public boolean apply(Event rawEvent) {
		try {
			if (rawEvent.type() == EventType.EVT_CACHE_OBJECT_REMOVED) {
				CacheEvent event = (CacheEvent) rawEvent;
				if (StringUtils.equals(ClientSessionManager.CLIENT_CACHE, event.cacheName())) {
					String clientId = event.key();
					ClientSession sessionOld = (ClientSession) event.oldValue();
					logger.info("Client {} expired", clientId);
					clientSessionManager.clientExit(clientId, sessionOld);
					clientSessionManager.getQueue(clientId).close();
					workerSessionManager.onClientRemove(clientId);
				} else if (StringUtils.equals(WorkerSessionManager.CLIENT_CACHE, event.cacheName())) {
					logger.info("worker {} expired", (String) event.key());
					WorkerSession session = (WorkerSession) event.oldValue();
					for (String clientId : session.allClientIds()) {
						workerSessionManager.getQueue(session, clientId).close();
						ClientSession clientSession = clientSessionManager.sessionByCid(clientId);
						if (clientSession != null) {
							ClientInfo clientInfo = clientInfoManager.findClientInfo(session.getWorkerId());
							clientSessionManager.getNotifyQueue(clientSession).put(Pair.of(WORKER_LOST, clientInfo));
						}
					}
					workerSessionManager.getNotifyQueue(session).close();
				}
			} else if (rawEvent.type() == EventType.EVT_NODE_JOINED || rawEvent.type() == EventType.EVT_NODE_LEFT
					|| rawEvent.type() == EventType.EVT_NODE_FAILED) {
				List<String> serverUrls = Lists.newArrayList();
				serverRegistry.servers().forEach(e -> serverUrls.add(e.getUrl()));
				for (WorkerSession worker : workerSessionManager.workers()) {
					workerSessionManager.getNotifyQueue(worker).put(Pair.of(SERVERS_CHANGED, serverUrls));
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return true;
	}

}
