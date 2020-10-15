package com.louyj.cottus.server.handler.worker;

import com.louyj.cottus.server.ServerRegistry;
import com.louyj.cottus.server.handler.IWorkerMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RsaExchangeMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.model.util.RsaUtils;

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
