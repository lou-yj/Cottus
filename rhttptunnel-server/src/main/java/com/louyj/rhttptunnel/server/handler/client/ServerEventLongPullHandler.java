package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.INTERRUPT;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.NoContentMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.ServerEventLongPullMessage;
import com.louyj.rhttptunnel.model.message.ServerEventMessage;
import com.louyj.rhttptunnel.model.message.consts.NotifyEventType;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.ClientSessionManager;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component("clientServerEventLongPullHandler")
public class ServerEventLongPullHandler implements IClientMessageHandler {

	@Value("${client.wait:60}")
	private int clientWait;

	@Autowired
	private ClientSessionManager clientSessionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ServerEventLongPullMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		try {
			Pair<NotifyEventType, Object> poll = clientSessionManager.getNotifyQueue(clientSession).poll(clientWait,
					TimeUnit.SECONDS);
			if (poll != null) {
				ServerEventMessage serverEventMessage = new ServerEventMessage(ClientInfo.SERVER,
						message.getExchangeId());
				serverEventMessage.setType(poll.getLeft());
				serverEventMessage.setEvent(poll.getRight());
				return serverEventMessage;
			}
			return new NoContentMessage(SERVER, message.getExchangeId());
		} catch (InterruptedException e) {
			return RejectMessage.sreason(message.getExchangeId(), INTERRUPT.reason());
		}
	}

}
