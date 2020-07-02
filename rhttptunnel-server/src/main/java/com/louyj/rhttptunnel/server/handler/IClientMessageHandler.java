package com.louyj.rhttptunnel.server.handler;

import java.util.List;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public interface IClientMessageHandler extends IMessageHandler {

	default boolean asyncMode() {
		return true;
	}

	BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception;

}
