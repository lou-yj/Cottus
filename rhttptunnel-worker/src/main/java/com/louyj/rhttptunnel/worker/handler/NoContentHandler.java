package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;

/**
 *
 * Created on 2020年3月15日
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
	public List<BaseMessage> handle(BaseMessage message) {
		return null;
	}

}
