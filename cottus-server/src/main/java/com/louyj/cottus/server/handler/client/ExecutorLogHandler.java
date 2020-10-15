package com.louyj.cottus.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import com.louyj.cottus.server.automation.AutomateManager;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.ExecutorLog;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogMessage;
import com.louyj.cottus.server.session.ClientSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ExecutorLogHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecutorLogMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ExecutorLogMessage logMessage = (ExecutorLogMessage) message;
		String executor = logMessage.getExecutor();
		String task = logMessage.getTask();
		List<ExecutorLog> executorLogs = automateManager.searchAuditLogs(logMessage.getTaskType(), executor, task,
				logMessage.getScheduleId());
		ExecutorLogMessage executorLogMessage = new ExecutorLogMessage(SERVER, message.getExchangeId());
		executorLogMessage.setLogs(executorLogs);
		return executorLogMessage;
	}

}
