package com.louyj.rhttptunnel.worker.handler;

import java.util.List;

import com.louyj.rhttptunnel.model.message.BaseMessage;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public interface IMessageHandler {

	Class<? extends BaseMessage> supportType();

	List<BaseMessage> handle(BaseMessage message) throws Exception;

}
