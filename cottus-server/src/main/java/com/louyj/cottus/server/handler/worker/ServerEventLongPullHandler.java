package com.louyj.cottus.server.handler.worker;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.INTERRUPT;

import java.util.concurrent.TimeUnit;

import com.louyj.cottus.server.handler.IWorkerMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.ServerEventLongPullMessage;
import com.louyj.rhttptunnel.model.message.ServerEventMessage;
import com.louyj.rhttptunnel.model.message.consts.NotifyEventType;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSessionManager;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component("workerServerEventLongPullHandler")
public class ServerEventLongPullHandler implements IWorkerMessageHandler {

	@Value("${worker.wait:60}")
	private int workerWait;

	@Autowired
	private WorkerSessionManager workerSessionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ServerEventLongPullMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		try {
			Pair<NotifyEventType, Object> poll = workerSessionManager.getNotifyQueue(workerSession).poll(workerWait,
					TimeUnit.SECONDS);
			if (poll != null) {
				ServerEventMessage serverEventMessage = new ServerEventMessage(SERVER, message.getExchangeId());
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
