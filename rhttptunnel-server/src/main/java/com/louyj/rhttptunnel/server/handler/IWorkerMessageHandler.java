package com.louyj.rhttptunnel.server.handler;

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
public interface IWorkerMessageHandler extends IMessageHandler {

	BaseMessage handle(WorkerSession workerSession, ClientSession clientSession, BaseMessage message) throws Exception;

}
