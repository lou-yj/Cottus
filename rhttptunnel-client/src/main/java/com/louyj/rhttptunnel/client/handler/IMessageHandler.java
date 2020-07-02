package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

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

	void handle(BaseMessage message, PrintStream writer) throws Exception;

}
