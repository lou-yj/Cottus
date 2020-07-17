package com.louyj.rhttptunnel.server.handler.worker;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.INTERRUPT;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.LongPullMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;
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
public class LongPullHandler implements IWorkerMessageHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${worker.wait:60}")
	private int workerWait;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return LongPullMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		LongPullMessage longPullMessage = (LongPullMessage) message;
		BlockingQueue<BaseMessage> messageQueue = workerSession.getMessageQueue(longPullMessage.getCid());
		try {
			BaseMessage poll = messageQueue.poll(workerWait, TimeUnit.SECONDS);
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
