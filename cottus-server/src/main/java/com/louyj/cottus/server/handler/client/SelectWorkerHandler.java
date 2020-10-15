package com.louyj.cottus.server.handler.client;

import java.util.List;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SelectWorkerMessage;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;

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
		clientSession.setWorkerIds(selectWorkerMessage.getWorkerIds());
		return AckMessage.sack(message.getExchangeId());
	}

}
