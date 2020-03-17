package com.louyj.rhttptunnel.server.client.handler;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.DiscoverMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class DiscoverHandler implements IClientMessageHandler {

	@Autowired
	private WorkerSessionManager workerSessionManager;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return DiscoverMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message)
			throws Exception {
		List<String> list = Lists.newArrayList();
		Collection<WorkerSession> workers = workerSessionManager.workers();
		list.add("INDEX\tHOST\tIP\tHEARTBEAT");
		int index = 1;
		for (WorkerSession worker : workers) {
			list.add(String.format("%s\t%s\t%s", index++, worker.getClientInfo().getHost(),
					worker.getClientInfo().getIp(),
					new DateTime(worker.getLastTime()).toString("yyyy-MM-dd HH:mm:ss")));
		}
		list.add("Found " + (index - 1) + " workes");
		AckMessage ackMessage = AckMessage.sack(message.getExchangeId()).withMessage(StringUtils.join(list, "\n"));
		return ackMessage;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

}
