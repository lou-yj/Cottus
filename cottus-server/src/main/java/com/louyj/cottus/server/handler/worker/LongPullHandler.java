package com.louyj.cottus.server.handler.worker;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.INTERRUPT;

import java.util.concurrent.TimeUnit;

import com.louyj.cottus.server.handler.IWorkerMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;
import com.louyj.cottus.server.session.WorkerSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.LongPullMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class LongPullHandler implements IWorkerMessageHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${worker.wait:60}")
	private int workerWait;
	@Autowired
	private WorkerSessionManager workerSessionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return LongPullMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		LongPullMessage longPullMessage = (LongPullMessage) message;
		try {
			BaseMessage poll = workerSessionManager.pollMessage(workerSession, longPullMessage.getCid(), workerWait,
					TimeUnit.SECONDS);
			if (poll != null) {
				return poll;
			}
			return new NoContentMessage(SERVER, message.getExchangeId());
		} catch (InterruptedException e) {
			logger.error("", e);
			return RejectMessage.sreason(message.getExchangeId(), INTERRUPT.reason());
		}
	}

}
