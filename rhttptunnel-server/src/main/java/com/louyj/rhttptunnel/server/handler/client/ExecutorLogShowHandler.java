package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.ExecutorLog;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorLogShowMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.TaskType;
import com.louyj.rhttptunnel.server.automation.AutomateManager;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ExecutorLogShowHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecutorLogShowMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ExecutorLogShowMessage logMessage = (ExecutorLogShowMessage) message;
		String executor = logMessage.getExecutor();
		String task = logMessage.getTask();
		List<ExecutorLog> executorLogs = automateManager.searchAuditLogs(TaskType.EXECUTOR, executor, task,
				logMessage.getScheduleId());
		ExecutorLogMessage executorLogMessage = new ExecutorLogMessage(SERVER, message.getExchangeId());
		executorLogMessage.setLogs(executorLogs);
		return executorLogMessage;
	}

}
