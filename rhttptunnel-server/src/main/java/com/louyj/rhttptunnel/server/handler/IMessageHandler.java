package com.louyj.rhttptunnel.server.handler;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.server.session.WorkerSession;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public interface IMessageHandler {

	Class<? extends BaseMessage> supportType();

	BaseMessage handle(WorkerSession session, BaseMessage message) throws Exception;

}
