package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.model.bean.automate.Executor;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.automate.ExecutorDetailMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ExecutorDetailHandler implements IMessageHandler {

	@Autowired
	protected ClientSession session;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ExecutorDetailMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		ExecutorDetailMessage itemsMessage = (ExecutorDetailMessage) message;
		Executor executor = itemsMessage.getExecutor();
		if (executor == null) {
			writer.println("No executor found");
			throw new EndOfMessageException();
		}
		String detail = JsonUtils.jackson().writerWithDefaultPrettyPrinter().writeValueAsString(executor);
		writer.println(detail);
		throw new EndOfMessageException();
	}

}
