package com.louyj.cottus.client.handler;

import java.io.PrintStream;

import com.louyj.cottus.client.exception.EndOfMessageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SecurityMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class SecurityHandler implements IMessageHandler {

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return SecurityMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		SecurityMessage registryMessage = (SecurityMessage) message;
		messageExchanger.setAesKey(registryMessage.getAesKey());
		throw new EndOfMessageException();
	}

}
