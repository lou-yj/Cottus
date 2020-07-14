package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellStartMessage;
import com.louyj.rhttptunnel.worker.shell.ShellManager;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShellStartHandler implements IMessageHandler {

	@Autowired
	private ShellManager shellManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellStartMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		shellManager.activeShell(message.getClient().identify());
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()).withMessage("Worker ready"));
	}

}
