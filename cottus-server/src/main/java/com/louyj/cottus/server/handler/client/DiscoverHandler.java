package com.louyj.cottus.server.handler.client;

import java.util.List;

import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.DiscoverMessage;
import com.louyj.rhttptunnel.model.message.WorkerListMessage;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSessionManager;

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
