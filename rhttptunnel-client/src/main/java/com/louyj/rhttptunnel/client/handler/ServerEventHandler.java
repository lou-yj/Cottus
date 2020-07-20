package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.ServerEventMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ServerEventHandler implements IMessageHandler {

	@Autowired
	private MessageExchanger messageExchanger;
	@Autowired
	private ClientSession clientSession;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ServerEventMessage.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ServerEventMessage eventMessage = (ServerEventMessage) message;
		if (eventMessage.getType() != null) {
			switch (eventMessage.getType()) {
			case WORKER_LOST:
				ClientInfo workerInfo = (ClientInfo) eventMessage.getEvent();
				writer.println(String.format("[WARN] Worker %s lost, remove from selected list", workerInfo.getHost()));
				clientSession.removeSelectedWorker(workerInfo.identify());
				break;
			case SERVERS_CHANGED:
				messageExchanger.setServerAddresses((List<String>) eventMessage.getEvent());
				break;
			default:
				break;
			}
		}
		throw new EndOfMessageException();
	}

}
