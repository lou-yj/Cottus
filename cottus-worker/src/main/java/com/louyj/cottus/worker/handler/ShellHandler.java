package com.louyj.cottus.worker.handler;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.SERVER_BAD_RESPONSE;
import static com.louyj.cottus.worker.ClientDetector.CLIENT;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.louyj.cottus.worker.shell.ShellManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.config.IConfigListener;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellResultMessage;
import com.louyj.cottus.worker.shell.ShellWrapper;
import com.louyj.cottus.worker.shell.ShellWrapper.ShellOutput;
import com.louyj.cottus.worker.shell.ShellWrapper.SubmitStatus;

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
	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ShellMessage shellMessage = (ShellMessage) message;
		ShellWrapper shellHolder = shellManager.activeShell(message.getClientId());
		Pair<SubmitStatus, String> submitStatus = shellHolder.submit(shellMessage.getMessage());
		if (StringUtils.equals(trim(shellMessage.getMessage()), "exit")) {
			return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
		}
		if (submitStatus.getLeft() != SubmitStatus.SUCCESS) {
			return Lists.newArrayList(RejectMessage.creason(CLIENT, message.getExchangeId(),
					"Current shell terminal is in " + submitStatus.getLeft() + " status"));
		}
		String cmdId = submitStatus.getRight();
		while (true) {
			ShellOutput fetchResult = shellHolder.fetchResult(cmdId);
			if (fetchResult.finished == false && isEmpty(fetchResult.out) && isEmpty(fetchResult.err)) {
				TimeUnit.MILLISECONDS.sleep(10);
				continue;
			}
			ShellResultMessage shellResultMessage = new ShellResultMessage(CLIENT, message.getExchangeId());
			shellResultMessage.setOut(fetchResult.out);
			shellResultMessage.setErr(fetchResult.err);
			BaseMessage ackMessage = messageExchanger.jsonPost(WORKER_EXCHANGE, shellResultMessage);
			if ((ackMessage instanceof AckMessage) == false) {
				shellHolder.close();
				return Lists.newArrayList(
						RejectMessage.creason(CLIENT, message.getExchangeId(), SERVER_BAD_RESPONSE.reason()));
			}
			if (fetchResult.finished) {
				return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
			}
		}
	}

	@Override
	public List<String> keys() {
		return Lists.newArrayList(SHELL_TIMEOUT);
	}

	@Override
	public String value(String clientId, String key) {
		ShellWrapper shellHolder = shellManager.getShell(clientId);
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
		ShellWrapper shellHolder = shellManager.getShell(clientId);
		if (shellHolder == null) {
			return;
		}
		shellHolder.setTimeout(NumberUtils.toInt(value, 60));
	}

}
