package com.louyj.cottus.server.handler.client;

import java.util.List;

import com.louyj.cottus.server.auth.UserPermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.user.UserDelMessage;
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
public class UserDelHandler implements IClientMessageHandler {

	@Autowired
	private UserPermissionManager userPermissionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return UserDelMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		UserDelMessage userDelMessage = (UserDelMessage) message;
		if (userPermissionManager.userExists(userDelMessage.getUserName()) == false) {
			return RejectMessage.sreason(message.getExchangeId(), "User not exists");
		}
		userPermissionManager.removeUser(userDelMessage.getUserName());
		return AckMessage.sack(message.getExchangeId());
	}

}
