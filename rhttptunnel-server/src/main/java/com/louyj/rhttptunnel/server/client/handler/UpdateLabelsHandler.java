package com.louyj.rhttptunnel.server.client.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.UpdateLabelMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.workerlabel.HostInfo;
import com.louyj.rhttptunnel.server.workerlabel.LabelRule;
import com.louyj.rhttptunnel.server.workerlabel.WorkerLabelManager;

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
			LabelRule labelRule = workerLabelManager.findRule(workerSession.getClientInfo().getHost(),
					workerSession.getClientInfo().getIp());
			if (labelRule == null) {
				labelRule = new LabelRule();
				labelRule.setHostInfo(
						new HostInfo(workerSession.getClientInfo().getHost(), workerSession.getClientInfo().getIp()));
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
