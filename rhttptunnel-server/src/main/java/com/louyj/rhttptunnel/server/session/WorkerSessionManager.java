package com.louyj.rhttptunnel.server.session;

import static com.louyj.rhttptunnel.model.message.consts.NotifyEventType.CLIENTS_CHANGED;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.bean.automate.IWorkerClientFilter;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.consts.NotifyEventType;
import com.louyj.rhttptunnel.server.IgniteRegistry;
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

	static final String CLIENT_CACHE = "workerCache";

	@Value("${worker.session.timeout:120}")
	private int workerSessionTimeout = 120;

	@Autowired
	private WorkerLabelManager workerLabelManager;
	@Autowired
	private ClientInfoManager clientInfoManager;
	@Autowired
	private IgniteRegistry igniteRegistry;

	private IgniteCache<String, WorkerSession> workerCache;

	@PostConstruct
	public void init() {
		workerCache = igniteRegistry.getOrCreateCache(CLIENT_CACHE, workerSessionTimeout, TimeUnit.SECONDS);
	}

	IgniteQueue<BaseMessage> getQueue(WorkerSession workerSession, String clientId) throws InterruptedException {
		if (workerSession.allClientIds().contains(clientId) == false) {
			workerSession.allClientIds().add(clientId);
			getNotifyQueue(workerSession).put(Pair.of(CLIENTS_CHANGED, workerSession.allClientIds()));
		}
		return igniteRegistry.<BaseMessage>queue("worker:" + workerSession.getWorkerId() + ":" + clientId, 100);
	}

	public IgniteQueue<Pair<NotifyEventType, Object>> getNotifyQueue(WorkerSession workerSession)
			throws InterruptedException {
		return igniteRegistry.<Pair<NotifyEventType, Object>>queue("workernotify:" + workerSession.getWorkerId(), 100);
	}

	public void putMessage(WorkerSession workerSession, String clientId, BaseMessage message)
			throws InterruptedException {
		getQueue(workerSession, clientId).put(message);
	}

	public BaseMessage pollMessage(WorkerSession workerSession, String clientId, int time, TimeUnit unit)
			throws InterruptedException {
		return getQueue(workerSession, clientId).poll(time, unit);
	}

	public void update(WorkerSession session) {
		if (session == null) {
			return;
		}
		workerCache.put(session.getWorkerId(), session);
	}

	public void update(List<WorkerSession> workerSessions) {
		if (workerSessions == null) {
			return;
		}
		for (WorkerSession workerSession : workerSessions) {
			update(workerSession);
		}
	}

	public List<WorkerInfo> filterWorkerInfos(Map<String, String> labels, Set<String> noLables) {
		List<WorkerInfo> workerInfos = Lists.newArrayList();
		for (WorkerSession worker : workers()) {
			ClientInfo clientInfo = clientInfoManager.findClientInfo(worker.getWorkerId());
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
			ClientInfo clientInfo = clientInfoManager.findClientInfo(worker.getWorkerId());
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
			ClientInfo clientInfo = clientInfoManager.findClientInfo(worker.getWorkerId());
			LabelRule labelRule = workerLabelManager.findRule(clientInfo);
			if (labelMatches(labelRule, labels, noLables) == false) {
				continue;
			}
			workerInfos.add(clientInfo);
		}
		return workerInfos;
	}

	public void update(String clientId) {
		WorkerSession session = workerCache.get(clientId);
		if (session == null) {
			session = new WorkerSession(clientId);
		}
		session.setLastTime(System.currentTimeMillis());
		workerCache.put(clientId, session);
	}

	public Collection<WorkerSession> workers() {
		List<WorkerSession> result = Lists.newArrayList();
		workerCache.forEach(e -> result.add(e.getValue()));
		return result;
	}

	public WorkerSession session(String clientId) {
		if (clientId == null) {
			return null;
		}
		return workerCache.get(clientId);
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
		workerCache.remove(clientId);
	}

	public void onClientRemove(String clientId) {
		workerCache.forEach(e -> {
			try {
				e.getValue().onClientRemove(this, clientId);
			} catch (InterruptedException ex) {
				logger.error("", ex);
			}
		});
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
