package com.louyj.rhttptunnel.server.client.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		DiscoverMessage discoverMessage = (DiscoverMessage) message;
		List<WorkerInfo> result = workerSessionManager.filterWorkerInfos(discoverMessage.getLabels(),
				discoverMessage.getNoLables());
		WorkerListMessage workersMessage = new WorkerListMessage(ClientInfo.SERVER, message.getExchangeId());
		workersMessage.setWorkers(result);
		return workersMessage;
	}

}
