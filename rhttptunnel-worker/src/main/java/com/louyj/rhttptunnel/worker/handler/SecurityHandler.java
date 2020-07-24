package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SecurityMessage;

/**
 *
 * Created on 2020年3月15日
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
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		SecurityMessage securityMessage = (SecurityMessage) message;
		messageExchanger.setAesKey(securityMessage.getAesKey());
		return Lists.newArrayList();
	}

}
