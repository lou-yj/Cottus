package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ConfigSetMessage;
import com.louyj.rhttptunnel.worker.ConfigManager;

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
		configManager.set(message.getClient().identify(), configSetMessage.getKey(), configSetMessage.getValue());
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
	}

}
