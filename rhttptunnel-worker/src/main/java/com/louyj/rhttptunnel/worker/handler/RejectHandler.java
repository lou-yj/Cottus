package com.louyj.rhttptunnel.worker.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
public class RejectHandler implements IMessageHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RejectMessage.class;
	}

	@Override
	public BaseMessage handle(BaseMessage message) throws Exception {
		logger.warn("Exchange reject by server, message {}", message);
		return null;
	}

}
