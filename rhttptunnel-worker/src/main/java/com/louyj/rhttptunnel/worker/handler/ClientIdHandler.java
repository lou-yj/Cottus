package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientIdMessage;
import com.louyj.rhttptunnel.worker.message.ClientWorkerManager;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ClientIdHandler implements IMessageHandler {

	@Autowired
	private ClientWorkerManager threadManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ClientIdMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ClientIdMessage clientIdMessage = (ClientIdMessage) message;
		threadManager.ensureThreads(clientIdMessage.getClientIds());
		return Lists.newArrayList();
	}

}
