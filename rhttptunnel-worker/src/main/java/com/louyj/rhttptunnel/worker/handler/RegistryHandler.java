package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RegistryMessage;
import com.louyj.rhttptunnel.model.util.RsaUtils;
import com.louyj.rhttptunnel.worker.ClientDetector;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class RegistryHandler implements IMessageHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RegistryMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		RegistryMessage registryMessage = (RegistryMessage) message;
		ClientDetector.CLIENT.setUuid(registryMessage.getRegistryClient().identify());
		logger.info("Client identfy {}", ClientDetector.CLIENT.identify());
		messageExchanger.setServerAddresses(registryMessage.getServers());
		messageExchanger.setAesKey(registryMessage.getAesKey());
		messageExchanger.setPublicKey(RsaUtils.loadKey(registryMessage.getPublicKey()));
		logger.info("Server addresses {}", registryMessage.getServers());
		return Lists.newArrayList();
	}

}
