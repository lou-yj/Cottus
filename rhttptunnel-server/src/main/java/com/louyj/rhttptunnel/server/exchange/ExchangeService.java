package com.louyj.rhttptunnel.server.exchange;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.AsyncExecAckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.handler.IMessageHandler;
import com.louyj.rhttptunnel.server.handler.IWorkerMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientSession;
import com.louyj.rhttptunnel.server.session.ClientSessionManager;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@RestController
@RequestMapping("exchange")
public class ExchangeService implements ApplicationContextAware, InitializingBean {

	Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;

	@Autowired
	private ClientSessionManager clientManager;
	@Autowired
	private WorkerSessionManager workerManager;
	@Autowired
	private ObjectMapper jackson;

	private ExecutorService executorService;

	private Map<Class<? extends BaseMessage>, IClientMessageHandler> clientHandlers;
	private Map<Class<? extends BaseMessage>, IWorkerMessageHandler> workerHandlers;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, IClientMessageHandler> clientTypes = applicationContext.getBeansOfType(IClientMessageHandler.class);
		clientHandlers = Maps.newHashMap();
		for (IClientMessageHandler messageHandler : clientTypes.values()) {
			Class<? extends BaseMessage> supportType = messageHandler.supportType();
			clientHandlers.put(supportType, messageHandler);
		}
		Map<String, IWorkerMessageHandler> workerTypes = applicationContext.getBeansOfType(IWorkerMessageHandler.class);
		workerHandlers = Maps.newHashMap();
		for (IWorkerMessageHandler messageHandler : workerTypes.values()) {
			Class<? extends BaseMessage> supportType = messageHandler.supportType();
			workerHandlers.put(supportType, messageHandler);
		}
		executorService = Executors.newFixedThreadPool(20);
	}

	@PostMapping(value = "client", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
	public String client(@RequestBody String data) throws Exception {
		return serializer(client(deserializer(data)));
	}

	@PostMapping(value = "worker", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
	public String worker(@RequestBody String data) throws Exception {
		return serializer(worker(deserializer(data)));
	}

	public BaseMessage client(BaseMessage message) throws Exception {
		Class<? extends BaseMessage> type = message.getClass();
		ClientInfo client = message.getClient();
		clientManager.update(client, message.getExchangeId());

		ClientSession clientSession = clientManager.session(client);
		WorkerSession workerSession = workerManager.session(clientSession.getWorkerInfo());
		IClientMessageHandler handler = clientHandlers.get(type);
		if (handler == null) {
			if (workerSession == null) {
				return RejectMessage.sreason(message.getExchangeId(), "Current worker offline.");
			}
			workerSession.putMessage(message);
			return AsyncExecAckMessage.sack(message.getExchangeId());
		} else {
			if (handler.asyncMode()) {
				ExchangeTask task = new ExchangeTask(handler, clientSession, workerSession, message);
				executorService.execute(task);
				return AsyncExecAckMessage.sack(message.getExchangeId());
			} else {
				return handler.handle(workerSession, clientSession, message);
			}
		}
	}

	public BaseMessage worker(BaseMessage message) throws Exception {
		Class<? extends BaseMessage> type = message.getClass();
		ClientInfo client = message.getClient();
		workerManager.update(client);

		WorkerSession workerSession = workerManager.session(client);
		ClientSession clientSession = clientManager.session(message.getExchangeId());

		IMessageHandler handler = workerHandlers.get(type);
		if (handler == null) {
			if (clientSession == null) {
				return RejectMessage.sreason(message.getExchangeId(), "Current client offline.");
			}
			clientSession.putMessage(message);
			return AckMessage.sack(message.getExchangeId());
		} else {
			return handler.handle(workerSession, clientSession, message);
		}

	}

	private BaseMessage deserializer(String data) throws IOException {
		return jackson.readValue(data, BaseMessage.class);
	}

	private String serializer(BaseMessage message) throws JsonProcessingException {
		return jackson.writeValueAsString(message);
	}

}