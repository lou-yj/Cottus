package com.louyj.rhttptunnel.server.client.handler;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.HeartBeatMessage;
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
public class HeartBeatHandler implements IClientMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return HeartBeatMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		return AckMessage.sack(message.getExchangeId());
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

}
