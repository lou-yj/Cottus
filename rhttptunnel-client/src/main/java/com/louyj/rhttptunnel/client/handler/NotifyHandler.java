package com.louyj.rhttptunnel.client.handler;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.util.LogUtils;
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
	public void handle(BaseMessage message) throws Exception {
		LogUtils.log(((NotifyMessage) message).getMessage());
	}

}
