package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.client.util.LogUtils;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AckHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AckMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		AckMessage ackMessage = (AckMessage) message;
		if (ackMessage.getMessage() != null) {
			LogUtils.printMessage(ackMessage.getMessage(), writer);
			throw new EndOfMessageException();
		} else
			throw new EndOfMessageException();
	}

}
