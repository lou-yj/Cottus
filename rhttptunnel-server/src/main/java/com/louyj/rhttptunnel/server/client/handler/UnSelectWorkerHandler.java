package com.louyj.rhttptunnel.server.client.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.UnSelectWorkerMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class UnSelectWorkerHandler implements IClientMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return UnSelectWorkerMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		clientSession.setWorkerInfos(null);
		return AckMessage.sack(message.getExchangeId());
	}

}
