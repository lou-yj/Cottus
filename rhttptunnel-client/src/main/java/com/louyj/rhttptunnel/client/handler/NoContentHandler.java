package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class NoContentHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return NoContentMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
	}

}
