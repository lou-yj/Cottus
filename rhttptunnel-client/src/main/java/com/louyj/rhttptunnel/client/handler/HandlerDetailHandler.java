package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.bean.automate.Handler;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.DescribeHandlerMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class HandlerDetailHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return DescribeHandlerMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		DescribeHandlerMessage itemsMessage = (DescribeHandlerMessage) message;
		Handler executor = itemsMessage.getHandler();
		if (executor == null) {
			writer.println("No handler found");
			throw new EndOfMessageException();
		}
		String detail = JsonUtils.jackson().writerWithDefaultPrettyPrinter().writeValueAsString(executor);
		writer.println(detail);
		throw new EndOfMessageException();
	}

}
