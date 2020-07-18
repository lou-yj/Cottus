package com.louyj.rhttptunnel.server.handler.worker;

import static com.louyj.rhttptunnel.model.message.consts.RejectReason.INTERRUPT;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientIdLongPullMessage;
import com.louyj.rhttptunnel.model.message.ClientIdMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.server.handler.IWorkerMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ClientIdLongPullHandler implements IWorkerMessageHandler {

	@Value("${worker.wait:60}")
	private int workerWait;

	@Autowired
	private WorkerSessionManager workerSessionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ClientIdLongPullMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		try {
			Set<String> poll = workerSessionManager.getNotifyQueue(workerSession).poll(workerWait, TimeUnit.SECONDS);
			if (poll == null) {
				poll = workerSession.allClientIds();
			}
			ClientIdMessage clientIdMessage = new ClientIdMessage(ClientInfo.SERVER, message.getExchangeId());
			clientIdMessage.setClientIds(poll);
			return clientIdMessage;
		} catch (InterruptedException e) {
			return RejectMessage.sreason(message.getExchangeId(), INTERRUPT.reason());
		}
	}

}
