package com.louyj.cottus.worker.handler;

import static com.louyj.cottus.worker.ClientDetector.CLIENT;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellEndMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShellEndHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellEndMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
	}

}
