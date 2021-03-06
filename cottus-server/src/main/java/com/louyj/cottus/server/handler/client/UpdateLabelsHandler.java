package com.louyj.cottus.server.handler.client;

import java.util.List;

import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import com.louyj.cottus.server.workerlabel.HostInfo;
import com.louyj.cottus.server.workerlabel.LabelRule;
import com.louyj.cottus.server.workerlabel.WorkerLabelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.label.UpdateLabelMessage;
import com.louyj.cottus.server.session.ClientInfoManager;
import com.louyj.cottus.server.session.ClientSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class UpdateLabelsHandler implements IClientMessageHandler {

	@Autowired
	private WorkerLabelManager workerLabelManager;
	@Autowired
	private ClientInfoManager clientInfoManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return UpdateLabelMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		UpdateLabelMessage updateLabelMessage = (UpdateLabelMessage) message;
		for (WorkerSession workerSession : workerSessions) {
			ClientInfo clientInfo = clientInfoManager.findClientInfo(workerSession.getWorkerId());
			LabelRule labelRule = workerLabelManager.findRule(clientInfo);
			if (labelRule == null) {
				labelRule = new LabelRule();
				labelRule.setHostInfo(new HostInfo(clientInfo.getHost(), clientInfo.getIp()));
			}
			for (String labelName : updateLabelMessage.getDelLabels()) {
				labelRule.getLabels().remove(labelName);
			}
			labelRule.getLabels().putAll(updateLabelMessage.getSetLabels());
			workerLabelManager.registryRule(labelRule);
		}
		return AckMessage.sack(message.getExchangeId());
	}

}
