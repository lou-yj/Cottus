package com.louyj.rhttptunnel.server.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.server.handler.IMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class ExchangeTask implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private IMessageHandler handler;
	private ClientSession clientSession;
	private WorkerSession workerSession;
	private BaseMessage message;

	public ExchangeTask(IMessageHandler handler, ClientSession clientSession, WorkerSession workerSession,
			BaseMessage message) {
		super();
		this.handler = handler;
		this.clientSession = clientSession;
		this.workerSession = workerSession;
		this.message = message;
	}

	@Override
	public void run() {
		try {
			BaseMessage baseMessage = handler.handle(workerSession, clientSession, message);
			if (baseMessage != null) {
				clientSession.getMessageQueue().add(baseMessage);
			}
		} catch (Exception e) {
			logger.error("Exceptioned exchange id {}", message.getExchangeId(), e);
			clientSession.getMessageQueue().add(RejectMessage.sreason(message.getExchangeId(),
					"Exception " + e.getClass().getSimpleName() + ":" + e.getMessage()));
		}
	}

}
