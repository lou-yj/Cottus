package com.louyj.rhttptunnel.server.handler;

import static com.louyj.rhttptunnel.model.message.status.RejectReason.INTERRUPT;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.LongPullMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.SleepMessage;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
public class LongPullHandler implements IMessageHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Class<? extends BaseMessage> supportType() {
		return LongPullMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession session, BaseMessage message) {
		LongPullMessage longPullMessage = (LongPullMessage) message;
		BlockingQueue<BaseMessage> messageQueue = session.getMessageQueue();
		try {
			BaseMessage poll = messageQueue.poll(longPullMessage.getSecond(), TimeUnit.SECONDS);
			if (poll != null) {
				return poll;
			}
			return new SleepMessage(ClientInfo.SERVER, 10);
		} catch (InterruptedException e) {
			logger.error("", e);
			return RejectMessage.sreason(message.getExchangeId(), INTERRUPT);
		}
	}

}
