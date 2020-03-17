package com.louyj.rhttptunnel.server.client.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.UnSelectWorkerMessage;
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
public class UnSelectWorkerHandler implements IClientMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return UnSelectWorkerMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		UnSelectWorkerMessage selectWorkerMessage = (UnSelectWorkerMessage) message;
		ClientInfo workerInfo = clientSession.getWorkerInfo();
		if (StringUtils.equals(workerInfo.identify(), selectWorkerMessage.getWorker().identify())) {
			clientSession.setWorkerInfo(null);
			return AckMessage.sack(message.getExchangeId());
		} else {
			return RejectMessage.sreason(message.getExchangeId(), "Unselected worker not matchs");
		}
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

}
