package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorItemsMessage;
import com.louyj.rhttptunnel.model.message.automate.ListExecutorsMessage;
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
public class ListExecutorsHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ListExecutorsMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		List<Executor> executors = automateManager.getExecutors();
		ExecutorItemsMessage itemsMessage = new ExecutorItemsMessage(SERVER, message.getExchangeId());
		itemsMessage.setExecutors(executors);
		return itemsMessage;
	}

}
