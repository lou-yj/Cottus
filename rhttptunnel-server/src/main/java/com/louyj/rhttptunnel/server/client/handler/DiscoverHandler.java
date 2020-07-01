package com.louyj.rhttptunnel.server.client.handler;

import java.util.Collection;
import java.util.List;

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

	@Override
	public Class<? extends BaseMessage> supportType() {
		return DiscoverMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		List<WorkerInfo> result = Lists.newArrayList();
		Collection<WorkerSession> workers = workerSessionManager.workers();
		for (WorkerSession worker : workers) {
			WorkerInfo info = new WorkerInfo();
			info.setClientInfo(worker.getClientInfo());
			result.add(info);
		}
		WorkerListMessage workersMessage = new WorkerListMessage(ClientInfo.SERVER, message.getExchangeId());
		workersMessage.setWorkers(result);
		return workersMessage;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

}
