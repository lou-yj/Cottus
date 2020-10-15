package com.louyj.cottus.server.handler.worker;

import com.louyj.cottus.server.handler.IWorkerMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SecurityMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.cottus.server.session.ClientSession;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component("workerSecurityHandler")
public class SecurityHandler implements IWorkerMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return SecurityMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		SecurityMessage securityMessage = (SecurityMessage) message;
		String aesKey = RandomStringUtils.random(10, true, true);
		workerSession.setAesKey(aesKey);
		securityMessage = JsonUtils.cloneObject(JsonUtils.jackson(), securityMessage);
		securityMessage.setAesKey(aesKey);
		return securityMessage;
	}

}
