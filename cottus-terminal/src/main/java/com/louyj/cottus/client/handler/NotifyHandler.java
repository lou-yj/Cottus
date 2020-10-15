package com.louyj.cottus.client.handler;

import java.io.PrintStream;

import org.springframework.stereotype.Component;

import com.louyj.cottus.client.util.LogUtils;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.NotifyMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class NotifyHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return NotifyMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		LogUtils.log(((NotifyMessage) message).getMessage(), writer);
	}

}
