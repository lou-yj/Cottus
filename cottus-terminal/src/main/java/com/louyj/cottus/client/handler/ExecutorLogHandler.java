package com.louyj.cottus.client.handler;

import java.io.PrintStream;
import java.util.List;

import com.louyj.cottus.client.ClientSession;
import com.louyj.cottus.client.exception.EndOfMessageException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.ExecutorLog;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ExecutorLogHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecutorLogMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ExecutorLogMessage logMessage = (ExecutorLogMessage) message;
		List<ExecutorLog> executorLogs = logMessage.getLogs();
		for (ExecutorLog executorLog : executorLogs) {
			if (StringUtils.isBlank(executorLog.getHost())) {
				continue;
			}
			writer.println(String.format("Worker %s[%s]", executorLog.getHost(), executorLog.getIp()));
			if (StringUtils.isNotBlank(executorLog.getStdout())) {
				writer.println(executorLog.getStdout());
			}
			if (StringUtils.isNotBlank(executorLog.getStderr())) {
				writer.println("================ERROR================");
				writer.println(executorLog.getStderr());
			}
		}
		throw new EndOfMessageException();
	}

}
