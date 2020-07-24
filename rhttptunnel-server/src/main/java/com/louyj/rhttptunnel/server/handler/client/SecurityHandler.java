package com.louyj.rhttptunnel.server.handler.client;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SecurityMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
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
@Component("clientSecurityHandler")
public class SecurityHandler implements IClientMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return SecurityMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		SecurityMessage securityMessage = (SecurityMessage) message;
		String aesKey = RandomStringUtils.random(10, true, true);
		clientSession.setAesKey(aesKey);
		securityMessage = JsonUtils.cloneObject(JsonUtils.jackson(), securityMessage);
		securityMessage.setAesKey(aesKey);
		return securityMessage;
	}

}
