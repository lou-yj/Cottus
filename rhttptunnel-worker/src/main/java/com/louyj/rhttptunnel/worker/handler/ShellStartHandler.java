package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellStartMessage;

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
	private ShellHandler shellHandler;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellStartMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		BaseMessage startShell = shellHandler.startShell(message);
		if (startShell == null) {
			return null;
		}
		return Lists.newArrayList(startShell);

	}

}
