package com.louyj.cottus.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import com.louyj.cottus.server.handler.IClientMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.ExecutorTaskRecord;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorRecordsMessage;
import com.louyj.cottus.server.automation.AutomateManager;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;

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
		return ExecutorRecordsMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		ExecutorRecordsMessage listMessage = (ExecutorRecordsMessage) message;
		String executor = listMessage.getExecutor();
		String task = listMessage.getTask();
		List<ExecutorTaskRecord> auditRecords = automateManager.searchAuditRecords(listMessage.getTaskType(), executor,
				task, 100);
		ExecutorRecordsMessage recordsMessage = new ExecutorRecordsMessage(SERVER, message.getExchangeId());
		recordsMessage.setRecords(auditRecords);
		return recordsMessage;
	}

}
