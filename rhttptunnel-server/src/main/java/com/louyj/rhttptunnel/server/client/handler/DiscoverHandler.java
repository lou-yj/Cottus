package com.louyj.rhttptunnel.server.client.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.DiscoverMessage;
import com.louyj.rhttptunnel.model.message.WorkerListMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;
import com.louyj.rhttptunnel.server.workerlabel.LabelRule;
import com.louyj.rhttptunnel.server.workerlabel.WorkerLabelManager;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class DiscoverHandler implements IClientMessageHandler {

	@Autowired
	private WorkerSessionManager workerSessionManager;

	@Autowired
	private WorkerLabelManager workerLabelManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return DiscoverMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		DiscoverMessage discoverMessage = (DiscoverMessage) message;
		List<WorkerInfo> result = Lists.newArrayList();
		Collection<WorkerSession> workers = workerSessionManager.workers();
		for (WorkerSession worker : workers) {
			LabelRule labelRule = workerLabelManager.findRule(worker.getClientInfo().getHost(),
					worker.getClientInfo().getIp());
			if (labelMatches(labelRule, discoverMessage) == false) {
				continue;
			}
			WorkerInfo info = new WorkerInfo();
			info.setClientInfo(worker.getClientInfo());
			info.setLabels(labelRule.getLabels());
			result.add(info);
		}
		WorkerListMessage workersMessage = new WorkerListMessage(ClientInfo.SERVER, message.getExchangeId());
		workersMessage.setWorkers(result);
		return workersMessage;
	}

	private boolean labelMatches(LabelRule labelRule, DiscoverMessage discoverMessage) {
		if (CollectionUtils.isNotEmpty(discoverMessage.getNoLables())) {
			for (String noLabel : discoverMessage.getNoLables()) {
				if (labelRule.getLabels().containsKey(noLabel)) {
					return false;
				}
			}
		}
		if (MapUtils.isNotEmpty(discoverMessage.getLabels())) {
			for (Entry<String, String> entry : discoverMessage.getLabels().entrySet()) {
				String value = labelRule.getLabels().get(entry.getKey());
				if (StringUtils.equals(value, entry.getValue()) == false) {
					return false;
				}
			}
		}
		return true;
	}

}
