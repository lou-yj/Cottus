package com.louyj.rhttptunnel.server.exchange;

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
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.AsyncExecAckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.status.IRejectReason;
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
@RestController("exchange")
public class ExchangeService implements ApplicationContextAware, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;

	@Autowired
	private ClientSessionManager clientManager;
	@Autowired
	private WorkerSessionManager workerManager;

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

	@PostMapping("client")
	public BaseMessage client(BaseMessage message) throws Exception {
		Class<? extends BaseMessage> type = message.getClass();
		logger.info("Rective client {} message, content {}", type.getSimpleName(), message);
		ClientInfo client = message.getClient();
		clientManager.update(client, message.getExchangeId());

		ClientSession clientSession = clientManager.session(client);
		WorkerSession workerSession = workerManager.session(clientSession.getWorkerInfo());
		if (workerSession == null) {
			return RejectMessage.sreason(message.getExchangeId(), IRejectReason.make("Current worker offline."));
		}
		IMessageHandler handler = clientHandlers.get(type);
		if (handler == null) {
			workerSession.putMessage(message);
			return AsyncExecAckMessage.sack(message.getExchangeId());
		} else {
			ExchangeTask task = new ExchangeTask(handler, clientSession, workerSession, message);
			executorService.execute(task);
			return AsyncExecAckMessage.sack(message.getExchangeId());
		}
	}

	@PostMapping("worker")
	public BaseMessage worker(BaseMessage message) throws Exception {
		Class<? extends BaseMessage> type = message.getClass();
		logger.info("Rective worker {} message, content {}", type.getSimpleName(), message);
		ClientInfo client = message.getClient();
		workerManager.update(client);

		WorkerSession workerSession = workerManager.session(client);
		ClientSession clientSession = clientManager.session(message.getExchangeId());
		if (clientSession == null) {
			return RejectMessage.sreason(message.getExchangeId(), IRejectReason.make("Current client offline."));
		}

		IMessageHandler handler = workerHandlers.get(type);
		if (handler == null) {
			clientSession.putMessage(message);
			return AckMessage.sack(message.getExchangeId());
		} else {
			return handler.handle(workerSession, clientSession, message);
		}
	}

}
