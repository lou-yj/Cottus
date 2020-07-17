package com.louyj.rhttptunnel.worker.message;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientIdLongPullMessage;
import com.louyj.rhttptunnel.model.message.RegistryMessage;
import com.louyj.rhttptunnel.worker.ClientDetector;

/**
 *
 * Created on 2020年3月23日
 *
 * @author Louyj
 *
 */
@Component
public class ClientDiscover extends Thread implements InitializingBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.start();
	}

	@Override
	public void run() {
		RegistryMessage registryMessage = new RegistryMessage(ClientDetector.CLIENT);
		registryMessage.setRegistryClient(ClientDetector.CLIENT);
		BaseMessage message = messageExchanger.jsonPost(WORKER_EXCHANGE, registryMessage);
		if ((message instanceof RegistryMessage) == false) {
			throw new RuntimeException("Registry failed");
		}
		MessageUtils.handle(message);
		while (true) {
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
	}

	private void doRun() throws JsonParseException, JsonMappingException, IOException {
		ClientIdLongPullMessage longPullMessage = new ClientIdLongPullMessage(ClientDetector.CLIENT);
		BaseMessage message = messageExchanger.jsonPost(WORKER_EXCHANGE, longPullMessage);
		MessageUtils.handle(message);
	}
}
