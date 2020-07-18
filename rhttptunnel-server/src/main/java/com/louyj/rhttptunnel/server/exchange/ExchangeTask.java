package com.louyj.rhttptunnel.server.exchange;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.ClientSessionManager;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ExchangeTask implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private IClientMessageHandler handler;
	private ClientSession clientSession;
	private WorkerSession workerSession;
	private BaseMessage message;
	private ClientSessionManager clientSessionManager;
	private WorkerSessionManager workerSessionManager;

	public ExchangeTask(IClientMessageHandler handler, ClientSession clientSession, WorkerSession workerSession,
			BaseMessage message, ClientSessionManager clientSessionManager, WorkerSessionManager workerSessionManager) {
		super();
		this.handler = handler;
		this.clientSession = clientSession;
		this.workerSession = workerSession;
		this.message = message;
		this.clientSessionManager = clientSessionManager;
		this.workerSessionManager = workerSessionManager;
	}

	@Override
	public void run() {
		try {
			BaseMessage baseMessage = handler.handle(Arrays.asList(workerSession), clientSession, message);
			clientSessionManager.update(clientSession);
			workerSessionManager.update(workerSession);
			if (baseMessage != null) {
				clientSessionManager.putMessage(clientSession.getClientId(), baseMessage);
			}
		} catch (Exception e) {
			logger.error("Exceptioned exchange id {}", message.getExchangeId(), e);
			RejectMessage rejectMessage = RejectMessage.sreason(message.getExchangeId(),
					"Exception " + e.getClass().getSimpleName() + ":" + e.getMessage());
			clientSessionManager.putMessage(clientSession.getClientId(), rejectMessage);
		}
	}

}
