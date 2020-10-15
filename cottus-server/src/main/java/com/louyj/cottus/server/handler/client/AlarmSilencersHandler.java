package com.louyj.cottus.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import com.louyj.cottus.server.automation.AlarmSilencer;
import com.louyj.cottus.server.automation.AutomateManager;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.AlarmSilencersMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.cottus.server.session.ClientSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class AlarmSilencersHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	private ObjectMapper jackson = JsonUtils.jackson();

	@Override
	public Class<? extends BaseMessage> supportType() {
		return AlarmSilencersMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		AlarmSilencersMessage itemsMessage = new AlarmSilencersMessage(SERVER, message.getExchangeId());
		List<AlarmSilencer> alarmSilencers = automateManager.getAlarmService().findAvalilAlarmSilencers();
		List<com.louyj.rhttptunnel.model.bean.automate.AlarmSilencer> items = Lists.newArrayList();
		for (AlarmSilencer alarmSilencer : alarmSilencers) {
			com.louyj.rhttptunnel.model.bean.automate.AlarmSilencer silencer = jackson.convertValue(alarmSilencer,
					com.louyj.rhttptunnel.model.bean.automate.AlarmSilencer.class);
			items.add(silencer);
		}
		itemsMessage.setSilencers(items);
		return itemsMessage;
	}

}
