package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.auth.RoleMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class RoleHandler implements IMessageHandler {

	@Autowired
	private ClientSession clientSession;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RoleMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		RoleMessage roleMessage = (RoleMessage) message;
		clientSession.setRole(roleMessage.getRole());
	}

}
