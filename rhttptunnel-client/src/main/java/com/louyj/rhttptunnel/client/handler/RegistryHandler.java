package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
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

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RegistryMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		RegistryMessage registryMessage = (RegistryMessage) message;
		ClientDetector.CLIENT.setUuid(registryMessage.getRegistryClient().identify());
		writer.println("Client identify " + ClientDetector.CLIENT.identify());
		throw new EndOfMessageException();
	}

}
