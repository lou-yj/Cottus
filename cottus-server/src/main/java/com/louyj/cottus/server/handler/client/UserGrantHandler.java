package com.louyj.cottus.server.handler.client;

import java.util.Collection;
import java.util.List;

import com.louyj.cottus.server.auth.UserPermissionManager;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Permission;
import com.louyj.rhttptunnel.model.bean.User;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.user.UserGrantMessage;
import com.louyj.cottus.server.session.ClientSession;

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
		return UserGrantMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		UserGrantMessage userAddMessage = (UserGrantMessage) message;
		User addUser = userAddMessage.getUser();
		if (clientSession.isSuperAdmin() == false) {
			String currentUserName = clientSession.getUserName();
			User currentUser = userPermissionManager.userById(currentUserName);
			if (currentUser == null) {
				return RejectMessage.sreason(message.getExchangeId(), "Operation has no permission");
			}
			Permission permission = userPermissionManager.permission(currentUser);
			Permission permission2 = userPermissionManager.permission(addUser);
			Collection<String> subtract = CollectionUtils.subtract(permission2.getCommands(), permission.getCommands());
			if (CollectionUtils.isNotEmpty(subtract)) {
				return RejectMessage.sreason(message.getExchangeId(), String
						.format("You don't have permission to grant %s commands", StringUtils.join(subtract, ",")));
			}
		}
		if (userPermissionManager.userExists(addUser.getName()) == false) {
			return RejectMessage.sreason(message.getExchangeId(), "No such user");
		}
		User addUserOld = userPermissionManager.userById(addUser.getName());
		addUser.setPassword(addUserOld.getPassword());
		userPermissionManager.upsertUser(userAddMessage.getUser());
		return AckMessage.sack(message.getExchangeId());
	}

}
