package com.louyj.cottus.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AsyncFetchMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.ClientSessionManager;
import com.louyj.cottus.server.session.WorkerSession;

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

	@Value("${client.wait:60}")
	private int clientWait;

	@Autowired
	private ClientSessionManager clientSessionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AsyncFetchMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		while (true) {
			BaseMessage poll = clientSessionManager.pollMessage(clientSession.getClientId(), clientWait, SECONDS);
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

}
