package com.louyj.rhttptunnel.server;

import static com.louyj.rhttptunnel.model.message.consts.RejectReason.INTERNEL_SERVER_ERROR;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.HeartBeatMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.exchange.ExchangeService;
import com.louyj.rhttptunnel.server.session.ClientInfoManager;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.ClientSessionManager;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
@Component
public class SystemClient extends TimerTask {

	public static interface ISystemClientListener {

		// null is all
		List<Class<? extends BaseMessage>> listenSendMessages();

		// null is all
		List<Class<? extends BaseMessage>> listenReceiveMessages();

		void onSendMessage(BaseMessage message, List<String> toWorkers);

		void onReceiveMessage(BaseMessage message);

	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ExchangeService exchangeService;
	@Autowired
	private ClientSessionManager clientSessionManager;
	@Autowired
	private List<ISystemClientListener> listeners = Lists.newArrayList();
	@Autowired
	private ClientInfoManager clientInfoManager;

	private ClientInfo systemClient;
	private Timer timer;
	private String exchangeId = UUID.randomUUID().toString();
	private ObjectMapper normalJackson = JsonUtils.jackson();

	@PostConstruct
	public void init() {

		systemClient = new ClientInfo("system", "system");
		clientInfoManager.registryClient(systemClient);

		timer = new Timer(true);
		timer.schedule(this, 0, 240_000);
		new Thread() {

			@Override
			public void run() {
				while (this.isInterrupted() == false) {
					try {
						ClientSession clientSession = session();
						if (clientSession == null) {
							TimeUnit.SECONDS.sleep(1);
							continue;
						}
						BlockingQueue<BaseMessage> messageQueue = clientSession.getMessageQueue();
						BaseMessage message = messageQueue.poll(1, TimeUnit.SECONDS);
						if (message == null) {
							continue;
						}
						for (ISystemClientListener listener : listeners) {
							if (listener.listenReceiveMessages() == null
									|| listener.listenReceiveMessages().contains(message.getClass())) {
								listener.onReceiveMessage(message);
							}
						}
						logMessage(message);
					} catch (InterruptedException e) {
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}.start();
	}

	public ClientSession session() {
		return clientSessionManager.sessionByCid(systemClient.identify());
	}

	public ClientInfo clientInfo() {
		return systemClient;
	}

	public BaseMessage exchange(BaseMessage message, List<String> toWorkers) {
		message = JsonUtils.cloneObject(normalJackson, message);
		for (ISystemClientListener listener : listeners) {
			if (listener.listenSendMessages() == null || listener.listenSendMessages().contains(message.getClass())) {
				listener.onSendMessage(message, toWorkers);
			}
		}
		message.setClientId(session().getClientId());
		message.setToWorkers(toWorkers);
		return exchange(message);
	}

	@Override
	public void run() {
		HeartBeatMessage message = new HeartBeatMessage(systemClient, exchangeId);
		exchange(message);
	}

	private BaseMessage exchange(BaseMessage message) {
		try {
			return exchangeService.client(message);
		} catch (Exception e) {
			logger.error("", e);
			RejectMessage ack = RejectMessage.sreason(message.getExchangeId(), INTERNEL_SERVER_ERROR.reason());
			return ack;
		}
	}

	@SuppressWarnings("rawtypes")
	private void logMessage(BaseMessage msg) throws JsonProcessingException {
		Map map = normalJackson.convertValue(msg, Map.class);
		map.remove("client");
		map.remove("exchangeId");
		String log = String.format("Receive [%s] %s", msg.getClass().getSimpleName(),
				normalJackson.writeValueAsString(map));
		logger.info(log);
	}

}
