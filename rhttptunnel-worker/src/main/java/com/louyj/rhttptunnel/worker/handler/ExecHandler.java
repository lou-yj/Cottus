package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.status.RejectReason.SERVER_BAD_RESPONSE;
import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ExecMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.ShellResultMessage;
import com.louyj.rhttptunnel.worker.shell.ShellManager;
import com.louyj.rhttptunnel.worker.shell.ShellWrapper;
import com.louyj.rhttptunnel.worker.shell.ShellWrapper.ShellOutput;
import com.louyj.rhttptunnel.worker.shell.ShellWrapper.SubmitStatus;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ExecHandler implements IMessageHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${work.directory}")
	private String workDirectory;
	@Autowired
	private ShellManager shellManager;
	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ExecMessage execMessage = (ExecMessage) message;
		String dir = execMessage.getWorkdir();
		if (StringUtils.isBlank(dir)) {
			dir = workDirectory;
		}
		String cmdLine = "cd '" + dir + "' && bash '" + execMessage.getPath() + "' " + execMessage.getArgs();
		logger.info("exec {}", cmdLine);
		ShellWrapper shellHolder = shellManager.activeShell(message.getClient().identify());
		Pair<SubmitStatus, String> submitStatus = shellHolder.submit(cmdLine);
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
			ShellResultMessage shellResultMessage = new ShellResultMessage(CLIENT);
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

}
