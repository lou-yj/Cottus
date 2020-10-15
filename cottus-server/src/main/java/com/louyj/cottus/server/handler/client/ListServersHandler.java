package com.louyj.cottus.server.handler.client;

import java.util.List;

import com.louyj.cottus.server.ServerRegistry;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.ListServersMessage;
import com.louyj.rhttptunnel.model.message.ServersMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ListServersHandler implements IClientMessageHandler {

	@Autowired
	private ServerRegistry serverRegistry;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ListServersMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ServersMessage serversMessage = new ServersMessage(ClientInfo.SERVER, message.getExchangeId());
		serversMessage.setServers(serverRegistry.servers());
		serversMessage.setMaster(serverRegistry.masterId());
		return serversMessage;
	}

}
