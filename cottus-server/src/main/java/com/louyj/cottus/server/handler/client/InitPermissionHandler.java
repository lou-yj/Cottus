package com.louyj.cottus.server.handler.client;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.louyj.cottus.server.auth.UserPermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Group;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.user.InitPermissionMessage;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class InitPermissionHandler implements IClientMessageHandler {

	@Autowired
	private UserPermissionManager userPermissionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return InitPermissionMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		InitPermissionMessage initMessage = (InitPermissionMessage) message;
		for (Entry<String, Set<String>> entry : initMessage.getCommandGroups().entrySet()) {
			Group group = new Group();
			group.setName(entry.getKey());
			group.setCommands(entry.getValue());
			userPermissionManager.upsertGroup(group);
		}
		return AckMessage.sack(message.getExchangeId());
	}

}
