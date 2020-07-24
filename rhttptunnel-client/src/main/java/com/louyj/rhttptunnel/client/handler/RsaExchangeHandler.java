package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RsaExchangeMessage;
import com.louyj.rhttptunnel.model.util.RsaUtils;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class RsaExchangeHandler implements IMessageHandler {

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RsaExchangeMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		RsaExchangeMessage registryMessage = (RsaExchangeMessage) message;
		messageExchanger.setPublicKey(RsaUtils.loadPublicKey(registryMessage.getPublicKey()));
		throw new EndOfMessageException();
	}

}
