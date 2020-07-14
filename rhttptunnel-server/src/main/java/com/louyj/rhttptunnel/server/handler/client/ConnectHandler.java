package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.RoleType;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ConnectMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.auth.RoleMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.user.UserManager;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ConnectHandler implements IClientMessageHandler {

	@Autowired
	private UserManager userManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ConnectMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ConnectMessage connectMessage = (ConnectMessage) message;
		RoleType roleType = userManager.verify(connectMessage.getUser(), connectMessage.getPassword());
		if (roleType != null) {
			RoleMessage roleMessage = new RoleMessage(SERVER, message.getExchangeId());
			roleMessage.setRole(roleType);
			return roleMessage;
		}
		return RejectMessage.sreason(message.getExchangeId(), "Auth failed");
	}

}
