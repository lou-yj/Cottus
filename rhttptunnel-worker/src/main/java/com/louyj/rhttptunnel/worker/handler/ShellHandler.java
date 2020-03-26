package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellMessage;
import com.louyj.rhttptunnel.worker.shell.ShellHolder;
import com.louyj.rhttptunnel.worker.shell.ShellManager;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShellHandler implements IMessageHandler {

	@Autowired
	private ShellManager shellManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ShellMessage shellMessage = (ShellMessage) message;
		ShellHolder shellHolder = shellManager.activeShell(message.getClient().identify());
		String output = shellHolder.exec(shellMessage.getMessage(), shellMessage.getTimeout());
		if (StringUtils.equals(shellMessage.getMessage(), "exit")) {
			return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
		}
		ShellMessage response = new ShellMessage(CLIENT, message.getExchangeId());
		response.setMessage(output);
		return Lists.newArrayList(response);
	}

}
