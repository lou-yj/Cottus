package com.louyj.cottus.server.handler;

import java.util.List;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.cottus.server.session.ClientSession;
import com.louyj.cottus.server.session.WorkerSession;

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

	default boolean needWorkerOnline() {
		return false;
	}

	BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception;

}
