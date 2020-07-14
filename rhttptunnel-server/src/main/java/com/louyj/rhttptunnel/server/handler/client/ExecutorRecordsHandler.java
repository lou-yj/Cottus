package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.ExecutorTaskRecord;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsListMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsMessage;
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
public class ExecutorRecordsHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecutorRecordsListMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ExecutorRecordsListMessage listMessage = (ExecutorRecordsListMessage) message;
		String executor = listMessage.getExecutor();
		String task = listMessage.getTask();
		List<ExecutorTaskRecord> auditRecords = automateManager.searchAuditRecords(TaskType.EXECUTOR, executor, task,
				100);
		ExecutorRecordsMessage recordsMessage = new ExecutorRecordsMessage(SERVER, message.getExchangeId());
		recordsMessage.setRecords(auditRecords);
		return recordsMessage;
	}

}
