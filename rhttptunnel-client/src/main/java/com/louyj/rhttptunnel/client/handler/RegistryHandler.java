package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.http.ExchangeContext;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RegistryMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class RegistryHandler implements IMessageHandler {

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RegistryMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		RegistryMessage registryMessage = (RegistryMessage) message;
		ClientDetector.CLIENT.setUuid(registryMessage.getRegistryClient().identify());
		messageExchanger.setServerAddresses(registryMessage.getServers());
		ThreadLocal<ExchangeContext> exchangeContext = messageExchanger.getExchangeContext();
		if (exchangeContext != null) {
			exchangeContext.get().setClientId(registryMessage.getRegistryClient().identify());
		}
		throw new EndOfMessageException();
	}

}
