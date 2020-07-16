package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.AlarmTriggeredRecord;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmerRecordsListMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmerRecordsMessage;
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
public class AlarmerRecordsHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmerRecordsListMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		AlarmerRecordsListMessage listMessage = (AlarmerRecordsListMessage) message;
		String alarmer = listMessage.getName();
		List<AlarmTriggeredRecord> alarmRecords = automateManager.getAlarmService().searchAlarmRecords(alarmer, 100);
		AlarmerRecordsMessage recordsMessage = new AlarmerRecordsMessage(SERVER, message.getExchangeId());
		recordsMessage.setRecords(alarmRecords);
		return recordsMessage;
	}

}
