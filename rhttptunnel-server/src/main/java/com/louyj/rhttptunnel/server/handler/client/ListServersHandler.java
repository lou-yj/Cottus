package com.louyj.rhttptunnel.server.handler.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.ListServersMessage;
import com.louyj.rhttptunnel.model.message.ServersMessage;
import com.louyj.rhttptunnel.server.ServerRegistry;
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
