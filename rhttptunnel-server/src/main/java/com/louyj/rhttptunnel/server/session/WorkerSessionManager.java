package com.louyj.rhttptunnel.server.session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.server.workerlabel.LabelRule;
import com.louyj.rhttptunnel.server.workerlabel.WorkerLabelManager;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class WorkerSessionManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private WorkerLabelManager workerLabelManager;

	private Cache<String, WorkerSession> workers = CacheBuilder.newBuilder().softValues()
			.expireAfterWrite(1, TimeUnit.MINUTES).build();

	public void update(WorkerSession session) {
		workers.put(session.getClientInfo().identify(), session);
	}

	public List<WorkerInfo> filterWorkerInfos(Map<String, String> labels, Set<String> noLables) {
		List<WorkerInfo> workerInfos = Lists.newArrayList();
		for (WorkerSession worker : workers()) {
			LabelRule labelRule = workerLabelManager.findRule(worker.getClientInfo());
			if (labelMatches(labelRule, labels, noLables) == false) {
				continue;
			}
			WorkerInfo info = new WorkerInfo();
			info.setClientInfo(worker.getClientInfo());
			info.setLabels(labelRule.getLabels());
			workerInfos.add(info);
		}
		return workerInfos;
	}

	public List<WorkerSession> filterWorkerSessions(Map<String, String> labels, Set<String> noLables) {
		List<WorkerSession> workerInfos = Lists.newArrayList();
		for (WorkerSession worker : workers()) {
			LabelRule labelRule = workerLabelManager.findRule(worker.getClientInfo());
			if (labelMatches(labelRule, labels, noLables) == false) {
				continue;
			}
			workerInfos.add(worker);
		}
		return workerInfos;
	}

	public List<ClientInfo> filterWorkerClients(Map<String, String> labels, Set<String> noLables) {
		List<ClientInfo> workerInfos = Lists.newArrayList();
		for (WorkerSession worker : workers()) {
			LabelRule labelRule = workerLabelManager.findRule(worker.getClientInfo());
			if (labelMatches(labelRule, labels, noLables) == false) {
				continue;
			}
			workerInfos.add(worker.getClientInfo());
		}
		return workerInfos;
	}

	public void update(ClientInfo client) {
		WorkerSession session = workers.getIfPresent(client.identify());
		if (session == null) {
			session = new WorkerSession(client);
		}
		session.setLastTime(System.currentTimeMillis());
		workers.put(client.identify(), session);
	}

	public Collection<WorkerSession> workers() {
		return workers.asMap().values();
	}

	public WorkerSession session(ClientInfo clientInfo) {
		if (clientInfo == null) {
			return null;
		}
		return workers.getIfPresent(clientInfo.identify());
	}

	public List<WorkerSession> sessions(List<ClientInfo> clientInfos) {
		if (clientInfos == null) {
			return Lists.newArrayList();
		}
		List<WorkerSession> workerSessions = Lists.newArrayList();
		for (ClientInfo clientInfo : clientInfos) {
			workerSessions.add(session(clientInfo));
		}
		return workerSessions;
	}

	public void remove(ClientInfo clientInfo) {
		workers.invalidate(clientInfo.identify());
	}

	public void onClientRemove(String clientId) {
		for (String workerId : workers.asMap().keySet()) {
			WorkerSession workerSession = workers.getIfPresent(workerId);
			if (workerSession == null) {
				continue;
			}
			try {
				workerSession.onClientRemove(clientId);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}

	private boolean labelMatches(LabelRule labelRule, Map<String, String> labels, Set<String> noLables) {
		if (CollectionUtils.isNotEmpty(noLables)) {
			for (String noLabel : noLables) {
				if (labelRule.getLabels().containsKey(noLabel)) {
					return false;
				}
			}
		}
		if (MapUtils.isNotEmpty(labels)) {
			for (Entry<String, String> entry : labels.entrySet()) {
				String value = labelRule.getLabels().get(entry.getKey());
				if (StringUtils.equals(value, entry.getValue()) == false) {
					return false;
				}
			}
		}
		return true;
	}

}
