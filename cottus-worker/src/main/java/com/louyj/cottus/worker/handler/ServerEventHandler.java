package com.louyj.cottus.worker.handler;

import java.util.List;

import com.louyj.cottus.worker.message.ClientWorkerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ServerEventMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ServerEventHandler implements IMessageHandler {

	@Autowired
	private ClientWorkerManager threadManager;
	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ServerEventMessage.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ServerEventMessage eventMessage = (ServerEventMessage) message;
		if (eventMessage.getType() != null) {
			switch (eventMessage.getType()) {
			case CLIENTS_CHANGED:
				threadManager.ensureThreads((List<String>) eventMessage.getEvent());
				break;
			case SERVERS_CHANGED:
				messageExchanger.setServerAddresses((List<String>) eventMessage.getEvent());
				break;
			default:
				break;
			}
		}
		return Lists.newArrayList();
	}

}
