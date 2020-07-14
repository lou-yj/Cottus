package com.louyj.rhttptunnel.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.automate.Handler;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.DescribeHandlerMessage;
import com.louyj.rhttptunnel.model.message.automate.HandlerDetailMessage;
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
public class DescribeHandlerHandler implements IClientMessageHandler {

	@Autowired
	private AutomateManager automateManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return DescribeHandlerMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		DescribeHandlerMessage describeMessage = (DescribeHandlerMessage) message;
		Handler handler = automateManager.getHandler(describeMessage.getName());
		HandlerDetailMessage detailMessage = new HandlerDetailMessage(SERVER, message.getExchangeId());
		detailMessage.setHandler(handler);
		return detailMessage;
	}

}