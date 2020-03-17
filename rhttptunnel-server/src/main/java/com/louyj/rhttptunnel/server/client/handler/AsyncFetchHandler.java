package com.louyj.rhttptunnel.server.client.handler;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AsyncFetchMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AsyncFetchHandler implements IClientMessageHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AsyncFetchMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		AsyncFetchMessage asyncFetchMessage = (AsyncFetchMessage) message;
		while (true) {
			BaseMessage poll = clientSession.getMessageQueue().poll(asyncFetchMessage.getSecond(), SECONDS);
			if (poll != null) {
				if (StringUtils.equals(message.getExchangeId(), poll.getExchangeId())) {
					return poll;
				} else {
					logger.error("Poll bad message, except exchange id {} actual {}, just discard it.",
							message.getExchangeId(), poll.getExchangeId());
				}
			} else {
				return new NoContentMessage(SERVER, message.getExchangeId());
			}
		}
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

}
