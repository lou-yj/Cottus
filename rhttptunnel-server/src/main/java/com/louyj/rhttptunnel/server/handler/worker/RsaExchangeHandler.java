package com.louyj.rhttptunnel.server.handler.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RsaExchangeMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.model.util.RsaUtils;
import com.louyj.rhttptunnel.server.ServerRegistry;
import com.louyj.rhttptunnel.server.handler.IWorkerMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component("workerRsaExchangeHandler")
public class RsaExchangeHandler implements IWorkerMessageHandler {

	@Autowired
	private ServerRegistry serverRegistry;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RsaExchangeMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		RsaExchangeMessage securityMessage = (RsaExchangeMessage) message;
		workerSession.setPublicKey(RsaUtils.loadPublicKey(securityMessage.getPublicKey()));
		securityMessage = JsonUtils.cloneObject(JsonUtils.jackson(), securityMessage);
		securityMessage.setPublicKey(RsaUtils.formatKey(serverRegistry.getPublicKey()));
		return securityMessage;
	}

}
