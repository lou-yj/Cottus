package com.louyj.cottus.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import com.louyj.cottus.server.auth.UserPermissionManager;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.user.UserListMessage;
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
public class UserListHandler implements IClientMessageHandler {

	@Autowired
	private UserPermissionManager userPermissionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return UserListMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		UserListMessage itemsMessage = new UserListMessage(SERVER, message.getExchangeId());
		itemsMessage.setUsers(userPermissionManager.users());
		return itemsMessage;
	}

}
