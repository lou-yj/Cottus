package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RsaExchangeMessage;
import com.louyj.rhttptunnel.model.util.RsaUtils;

/**
 *
 * Created on 2020年3月15日
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
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		RsaExchangeMessage rsaMessage = (RsaExchangeMessage) message;
		messageExchanger.setPublicKey(RsaUtils.loadPublicKey(rsaMessage.getPublicKey()));
		return Lists.newArrayList();
	}

}
