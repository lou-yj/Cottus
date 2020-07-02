package com.louyj.rhttptunnel.server.client.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SelectWorkerMessage;
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
public class SelectWorkerHandler implements IClientMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return SelectWorkerMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		SelectWorkerMessage selectWorkerMessage = (SelectWorkerMessage) message;
		clientSession.setWorkerInfos(selectWorkerMessage.getWorkers());
		return AckMessage.sack(message.getExchangeId());
	}

}
