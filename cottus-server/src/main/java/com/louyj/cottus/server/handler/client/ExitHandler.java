package com.louyj.cottus.server.handler.client;

import java.util.List;

import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.ClientSessionManager;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ExitMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ExitHandler implements IClientMessageHandler {

	@Autowired
	private ClientSessionManager clientSessionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExitMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ClientSession session = clientSessionManager.sessionByCid(message.getClientId());
		clientSessionManager.clientExit(message.getClientId(), session);
		return AckMessage.sack(message.getExchangeId());
	}

}
