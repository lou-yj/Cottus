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
import com.louyj.rhttptunnel.model.bean.automate.IWorkerClientFilter;
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
public class WorkerSessionManager implements IWorkerClientFilter {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private WorkerLabelManager workerLabelManager;
	@Autowired
	private ClientInfoManager clientInfoManager;

	private Cache<String, WorkerSession> workers = CacheBuilder.newBuilder().softValues()
			.expireAfterWrite(5, TimeUnit.MINUTES).build();

	public void update(WorkerSession session) {
		workers.put(session.getClientId(), session);
	}

	public List<WorkerInfo> filterWorkerInfos(Map<String, String> labels, Set<String> noLables) {
		List<WorkerInfo> workerInfos = Lists.newArrayList();
		for (WorkerSession worker : workers()) {
			ClientInfo clientInfo = clientInfoManager.findClientInfo(worker.getClientId());
			LabelRule labelRule = workerLabelManager.findRule(clientInfo);
			if (labelMatches(labelRule, labels, noLables) == false) {
				continue;
			}
			WorkerInfo info = new WorkerInfo();
			info.setClientInfo(clientInfo);
			info.setLabels(labelRule.getLabels());
			workerInfos.add(info);
		}
		return workerInfos;
	}

	public List<WorkerSession> filterWorkerSessions(Map<String, String> labels, Set<String> noLables) {
		List<WorkerSession> workerInfos = Lists.newArrayList();
		for (WorkerSession worker : workers()) {
			ClientInfo clientInfo = clientInfoManager.findClientInfo(worker.getClientId());
			LabelRule labelRule = workerLabelManager.findRule(clientInfo);
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
			ClientInfo clientInfo = clientInfoManager.findClientInfo(worker.getClientId());
			LabelRule labelRule = workerLabelManager.findRule(clientInfo);
			if (labelMatches(labelRule, labels, noLables) == false) {
				continue;
			}
			workerInfos.add(clientInfo);
		}
		return workerInfos;
	}

	public void update(String clientId) {
		WorkerSession session = workers.getIfPresent(clientId);
		if (session == null) {
			session = new WorkerSession(clientId);
		}
		session.setLastTime(System.currentTimeMillis());
		workers.put(clientId, session);
	}

	public Collection<WorkerSession> workers() {
		return workers.asMap().values();
	}

	public WorkerSession session(String clientId) {
		if (clientId == null) {
			return null;
		}
		return workers.getIfPresent(clientId);
	}

	public List<WorkerSession> sessions(List<String> clientIds) {
		if (clientIds == null) {
			return Lists.newArrayList();
		}
		List<WorkerSession> workerSessions = Lists.newArrayList();
		for (String clientId : clientIds) {
			WorkerSession session = session(clientId);
			if (session == null) {
				continue;
			}
			workerSessions.add(session);
		}
		return workerSessions;
	}

	public void remove(String clientId) {
		workers.invalidate(clientId);
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
