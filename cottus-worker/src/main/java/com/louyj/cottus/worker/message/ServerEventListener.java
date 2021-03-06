package com.louyj.cottus.worker.message;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;

import java.io.IOException;
import java.security.Key;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.louyj.rhttptunnel.model.bean.Pair;
import com.louyj.rhttptunnel.model.http.ExchangeContext;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RegistryMessage;
import com.louyj.rhttptunnel.model.message.RsaExchangeMessage;
import com.louyj.rhttptunnel.model.message.SecurityMessage;
import com.louyj.rhttptunnel.model.message.ServerEventLongPullMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.model.util.RsaUtils;
import com.louyj.cottus.worker.ClientDetector;

/**
 *
 * Created on 2020年3月23日
 *
 * @author Louyj
 *
 */
@Component
public class ServerEventListener extends Thread implements InitializingBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.start();
	}

	@Override
	public void run() {
		try {
			ExchangeContext exchangeContext = new ExchangeContext();
			exchangeContext.setClientId(ClientDetector.CLIENT.identify());
			exchangeContext.setCommand("heartbeat");
			messageExchanger.getExchangeContext().set(exchangeContext);

			Pair<Key, Key> keyPair = RsaUtils.genKeyPair();
			Pair<String, String> stringKeyPair = RsaUtils.stringKeyPair(keyPair);

			RegistryMessage registryMessage = new RegistryMessage(ClientDetector.CLIENT);
			registryMessage.setRegistryClient(ClientDetector.CLIENT);
			BaseMessage message = messageExchanger.jsonPost(WORKER_EXCHANGE, registryMessage);
			if ((message instanceof RegistryMessage) == false) {
				logger.warn("Registry failed with response {}", JsonUtils.gson().toJson(message));
				throw new RuntimeException("Registry failed");
			}
			MessageUtils.handle(message);
			RsaExchangeMessage rsaExchangeMessage = new RsaExchangeMessage(ClientDetector.CLIENT);
			rsaExchangeMessage.setPublicKey(stringKeyPair.getRight());
			message = messageExchanger.jsonPost(WORKER_EXCHANGE, rsaExchangeMessage);
			if ((message instanceof RsaExchangeMessage) == false) {
				logger.warn("Rsa exchange failed with response {}", JsonUtils.gson().toJson(message));
				throw new RuntimeException("Security exchange failed");
			}
			MessageUtils.handle(message);
			messageExchanger.setPrivateKey(keyPair.getLeft());
			SecurityMessage securityMessage = new SecurityMessage(ClientDetector.CLIENT);
			message = messageExchanger.jsonPost(WORKER_EXCHANGE, securityMessage);
			if ((message instanceof SecurityMessage) == false) {
				logger.warn("Security exchange failed with response {}", JsonUtils.gson().toJson(message));
				throw new RuntimeException("Security exchange failed");
			}
			MessageUtils.handle(message);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		ServerEventLongPullMessage longPullMessage = new ServerEventLongPullMessage(ClientDetector.CLIENT);
		BaseMessage message = messageExchanger.jsonPost(WORKER_EXCHANGE, longPullMessage);
		MessageUtils.handle(message);
	}
}
