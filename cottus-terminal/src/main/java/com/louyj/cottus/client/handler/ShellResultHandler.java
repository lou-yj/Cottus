package com.louyj.cottus.client.handler;

import java.io.PrintStream;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellResultMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ShellResultHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellResultMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ShellResultMessage shellResultMessage = (ShellResultMessage) message;
		if (CollectionUtils.isNotEmpty(shellResultMessage.getErr())) {
			for (String line : shellResultMessage.getErr()) {
				writer.println(line);
			}
		}
		if (CollectionUtils.isNotEmpty(shellResultMessage.getOut())) {
			for (String line : shellResultMessage.getOut()) {
				writer.println(line);
			}
		}
	}

}
