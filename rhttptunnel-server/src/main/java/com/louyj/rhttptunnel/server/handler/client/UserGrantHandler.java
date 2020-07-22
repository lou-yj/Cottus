package com.louyj.rhttptunnel.server.handler.client;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Permission;
import com.louyj.rhttptunnel.model.bean.User;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.user.UserAddMessage;
import com.louyj.rhttptunnel.server.auth.UserPermissionManager;
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
public class UserGrantHandler implements IClientMessageHandler {

	@Autowired
	private UserPermissionManager userPermissionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return UserAddMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		UserAddMessage userAddMessage = (UserAddMessage) message;
		String currentUserName = clientSession.getUserName();
		User currentUser = userPermissionManager.userById(currentUserName);
		if (currentUser == null) {
			return RejectMessage.sreason(message.getExchangeId(), "Operation has no permission");
		}
		User addUser = userAddMessage.getUser();
		if (userPermissionManager.userExists(addUser.getName()) == false) {
			return RejectMessage.sreason(message.getExchangeId(), "No such user");
		}
		Permission permission = userPermissionManager.permission(currentUser);
		Permission permission2 = userPermissionManager.permission(addUser);
		Collection<String> subtract = CollectionUtils.subtract(permission2.getCommands(), permission.getCommands());
		if (CollectionUtils.isNotEmpty(subtract)) {
			return RejectMessage.sreason(message.getExchangeId(),
					String.format("You don't have permission to grant %s commands", StringUtils.join(subtract, ",")));
		}
		User addUserOld = userPermissionManager.userById(addUser.getName());
		addUser.setPassword(addUserOld.getPassword());
		userPermissionManager.upsertUser(userAddMessage.getUser());
		return AckMessage.sack(message.getExchangeId());
	}

}
