package com.louyj.cottus.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import com.louyj.cottus.server.automation.AutomateManager;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmInhibitorsMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AlarmInhibitorsHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmInhibitorsMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		AlarmInhibitorsMessage itemsMessage = new AlarmInhibitorsMessage(SERVER, message.getExchangeId());
		itemsMessage.setInhibitors(automateManager.getAlarmInhibitors());
		return itemsMessage;
	}

}
