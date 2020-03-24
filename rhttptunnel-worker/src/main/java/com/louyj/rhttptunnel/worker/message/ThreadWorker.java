package com.louyj.rhttptunnel.worker.message;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.LongPullMessage;
import com.louyj.rhttptunnel.worker.ClientDetector;
import com.louyj.rhttptunnel.worker.handler.IClientCloseable;
import com.louyj.rhttptunnel.worker.handler.IMessageHandler;

/**
 *
 * Created on 2020年3月23日
 *
 * @author Louyj
 *
 */
public class ThreadWorker extends Thread {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String clientId;
	private MessageExchanger messageExchanger;

	private boolean shouldBreak = false;

	public ThreadWorker(String clientId, MessageExchanger messageExchanger) {
		super();
		this.clientId = clientId;
		this.messageExchanger = messageExchanger;
	}

	@Override
	public void run() {
		logger.info("Start worker thread response for client {}", clientId);
		while (!shouldBreak) {
			try {
				doRun();
			} catch (Exception e) {
				logger.error("", e);
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e1) {
				}
			}
		}
		logger.info("Stop worker thread response for client {}", clientId);
		Map<Class<? extends BaseMessage>, IMessageHandler> messageHandlers = MessageUtils.getMessageHandlers();
		for (IMessageHandler handler : messageHandlers.values()) {
			if (handler instanceof IClientCloseable) {
				try {
					logger.info("Close client resource using {} handler.", handler.getClass().getName());
					((IClientCloseable) handler).close(clientId);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		logger.info("Client resource closed");
	}

	public boolean isShouldBreak() {
		return shouldBreak;
	}

	public void setShouldBreak(boolean shouldBreak) {
		this.shouldBreak = shouldBreak;
	}

	private void doRun() throws JsonParseException, JsonMappingException, IOException {
		LongPullMessage longPullMessage = new LongPullMessage(ClientDetector.CLIENT, clientId);
		BaseMessage taskMessage = messageExchanger.jsonPost(WORKER_EXCHANGE, longPullMessage);
		MessageUtils.handle(taskMessage);
	}

}
