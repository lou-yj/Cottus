package com.louyj.rhttptunnel.server.session;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.events.CacheEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.lang.IgnitePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.server.IgniteRegistry;

@Component
public class IgniteExpireListener implements IgnitePredicate<CacheEvent> {

	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IgniteRegistry igniteRegistry;
	@Autowired
	private ClientSessionManager clientSessionManager;
	@Autowired
	private WorkerSessionManager workerSessionManager;

	@PostConstruct
	public void init() {
		igniteRegistry.localListen(this, EventType.EVT_CACHE_OBJECT_REMOVED);
	}

	@Override
	public boolean apply(CacheEvent event) {
		try {
			if (event.type() == EventType.EVT_CACHE_OBJECT_REMOVED) {
				if (StringUtils.equals(ClientSessionManager.CLIENT_CACHE, event.cacheName())) {
					String clientId = event.key();
					clientSessionManager.clientExit(clientId, (ClientSession) event.oldValue());
					clientSessionManager.getQueue(clientId).close();
					workerSessionManager.onClientRemove(clientId);
				} else if (StringUtils.equals(WorkerSessionManager.CLIENT_CACHE, event.cacheName())) {
					WorkerSession session = (WorkerSession) event.oldValue();
					for (String clientId : session.allClientIds()) {
						workerSessionManager.getQueue(session, clientId).close();
					}
					workerSessionManager.getNotifyQueue(session).close();
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return true;
	}

}
