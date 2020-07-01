package com.louyj.rhttptunnel.client.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.WorkerListMessage;

import de.vandermeer.asciitable.AsciiTable;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class WorkerListHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return WorkerListMessage.class;
	}

	@Override
	public void handle(BaseMessage message) throws Exception {
		WorkerListMessage listMessage = (WorkerListMessage) message;
		AsciiTable at = new AsciiTable();
		at.addRule();
		at.addRow("INDEX", "HOST", "IP", "UPTIME");
		int index = 1;
		for (WorkerInfo worker : listMessage.getWorkers()) {
			ClientInfo clientInfo = worker.getClientInfo();
			at.addRule();
			at.addRow(index++, clientInfo.getHost(), clientInfo.getIp(), clientInfo.getUptime());
		}
		at.addRule();
		String rend = at.render();
		System.out.println(rend);
		System.out.println("Found " + (index - 1) + " workes");
		session.setDiscoverWorkers(listMessage.getWorkers());
		throw new EndOfMessageException();
	}

}
