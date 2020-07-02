package com.louyj.rhttptunnel.client.handler;

import static com.louyj.rhttptunnel.client.consts.Status.FAILED;

import java.io.PrintStream;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.client.util.LogUtils;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class RejectHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RejectMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		RejectMessage rejectMessage = (RejectMessage) message;
		LogUtils.serverReject(rejectMessage, writer);
		throw new EndOfMessageException(FAILED);
	}

}
