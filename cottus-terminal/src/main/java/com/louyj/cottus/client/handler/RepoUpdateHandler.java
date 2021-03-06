package com.louyj.cottus.client.handler;

import java.io.PrintStream;

import com.louyj.cottus.client.util.LogUtils;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoUpdateMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class RepoUpdateHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RepoUpdateMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		RepoUpdateMessage repoUpdateMessage = (RepoUpdateMessage) message;
		LogUtils.printMessage(repoUpdateMessage.getMessage(), writer);
	}

}
