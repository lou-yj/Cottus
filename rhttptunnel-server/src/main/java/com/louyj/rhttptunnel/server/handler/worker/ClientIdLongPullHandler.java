package com.louyj.rhttptunnel.server.handler.worker;

import static com.louyj.rhttptunnel.model.message.status.RejectReason.INTERRUPT;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientIdLongPullMessage;
import com.louyj.rhttptunnel.model.message.ClientIdMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.server.handler.IWorkerMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ClientIdLongPullHandler implements IWorkerMessageHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ClientIdLongPullMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ClientIdLongPullMessage longPullMessage = (ClientIdLongPullMessage) message;
		BlockingQueue<Set<String>> clientIdQueue = workerSession.getClientIdQueue();
		try {
			Set<String> poll = clientIdQueue.poll(longPullMessage.getSecond(), TimeUnit.SECONDS);
			if (poll == null) {
				poll = workerSession.allClientIds();
			}
			ClientIdMessage clientIdMessage = new ClientIdMessage(ClientInfo.SERVER, message.getExchangeId());
			clientIdMessage.setClientIds(poll);
			return clientIdMessage;
		} catch (InterruptedException e) {
			logger.error("", e);
			return RejectMessage.sreason(message.getExchangeId(), INTERRUPT.reason());
		}
	}

}
