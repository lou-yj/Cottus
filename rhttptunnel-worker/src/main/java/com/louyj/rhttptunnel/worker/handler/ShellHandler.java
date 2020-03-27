package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellMessage;
import com.louyj.rhttptunnel.worker.ConfigManager.IConfigListener;
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
public class ShellHandler implements IMessageHandler, IConfigListener {

	private static final String SHELL_TIMEOUT = "shell.timeout";

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
		String output = shellHolder.exec(shellMessage.getMessage());
		if (StringUtils.equals(shellMessage.getMessage(), "exit")) {
			return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
		}
		ShellMessage response = new ShellMessage(CLIENT, message.getExchangeId());
		response.setMessage(output);
		return Lists.newArrayList(response);
	}

	@Override
	public List<String> keys() {
		return Lists.newArrayList(SHELL_TIMEOUT);
	}

	@Override
	public String value(String clientId, String key) {
		ShellHolder shellHolder = shellManager.getShell(clientId);
		if (shellHolder == null) {
			return EMPTY;
		}
		return String.valueOf(shellHolder.getTimeout());
	}

	@Override
	public void onChanged(String clientId, String key, String value) {
		if (StringUtils.equals(key, SHELL_TIMEOUT) == false) {
			return;
		}
		ShellHolder shellHolder = shellManager.getShell(clientId);
		if (shellHolder == null) {
			return;
		}
		shellHolder.setTimeout(NumberUtils.toInt(value, 60));
	}

}
