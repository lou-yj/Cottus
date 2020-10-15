package com.louyj.cottus.worker.handler;

import static com.louyj.cottus.worker.ClientDetector.CLIENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.config.ConfigSetMessage;
import com.louyj.cottus.worker.ConfigManager;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ConfigSetHandler implements IMessageHandler {

	@Autowired
	private ConfigManager configManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ConfigSetMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ConfigSetMessage configSetMessage = (ConfigSetMessage) message;
		configManager.set(message.getClientId(), configSetMessage.getKey(), configSetMessage.getValue());
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
	}

}
