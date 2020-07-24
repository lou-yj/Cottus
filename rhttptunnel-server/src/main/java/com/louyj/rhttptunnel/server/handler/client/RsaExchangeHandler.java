package com.louyj.rhttptunnel.server.handler.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RsaExchangeMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.model.util.RsaUtils;
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
@Component("clientRsaExchangeHandler")
public class RsaExchangeHandler implements IClientMessageHandler {

	@Autowired
	private ServerRegistry serverRegistry;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RsaExchangeMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		RsaExchangeMessage securityMessage = (RsaExchangeMessage) message;
		clientSession.setPublicKey(RsaUtils.loadPublicKey(securityMessage.getPublicKey()));

		securityMessage = JsonUtils.cloneObject(JsonUtils.jackson(), securityMessage);
		securityMessage.setPublicKey(RsaUtils.formatKey(serverRegistry.getPublicKey()));
		return securityMessage;
	}

}
