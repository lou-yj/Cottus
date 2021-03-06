package com.louyj.cottus.worker.message;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.louyj.rhttptunnel.model.http.ExchangeContext;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.LongPullMessage;
import com.louyj.cottus.worker.ClientDetector;
import com.louyj.cottus.worker.shell.ShellManager;

/**
 *
 * Created on 2020年3月23日
 *
 * @author Louyj
 *
 */
public class ClientWorker extends Thread {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String clientId;
	private MessageExchanger messageExchanger;
	private ShellManager shellManager;

	private boolean shouldBreak = false;

	public ClientWorker(String clientId, MessageExchanger messageExchanger, ShellManager shellManager) {
		super();
		this.clientId = clientId;
		this.messageExchanger = messageExchanger;
		this.shellManager = shellManager;
	}

	@Override
	public void run() {
		logger.info("Start worker thread response for client {}", clientId);
		ExchangeContext exchangeContext = new ExchangeContext();
		exchangeContext.setClientId(ClientDetector.CLIENT.identify());
		exchangeContext.setCommand("heartbeat");
		messageExchanger.getExchangeContext().set(exchangeContext);
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
		shellManager.destoryShell(clientId);
		logger.info("Shell destoried.");
		logger.info("Stop worker thread response for client {}", clientId);
	}

	public boolean isShouldBreak() {
		return shouldBreak;
	}

	public void setShouldBreak(boolean shouldBreak) {
		this.shouldBreak = shouldBreak;
		logger.info("Client worker {} should break", clientId);
	}

	private void doRun() throws JsonParseException, JsonMappingException, IOException {
		LongPullMessage longPullMessage = new LongPullMessage(ClientDetector.CLIENT, clientId);
		BaseMessage taskMessage = messageExchanger.jsonPost(WORKER_EXCHANGE, longPullMessage);
		MessageUtils.handle(taskMessage);
	}

}
