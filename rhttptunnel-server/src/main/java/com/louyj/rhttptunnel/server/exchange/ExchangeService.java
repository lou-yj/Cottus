package com.louyj.rhttptunnel.server.exchange;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
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
import com.louyj.rhttptunnel.model.annotation.NoLogFields;
import com.louyj.rhttptunnel.model.annotation.NoLogMessage;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.AsyncExecAckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RegistryMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.server.ServerRegistry;
import com.louyj.rhttptunnel.server.handler.IClientMessageHandler;
import com.louyj.rhttptunnel.server.handler.IWorkerMessageHandler;
import com.louyj.rhttptunnel.server.session.ClientInfoManager;
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
	@Autowired
	private ClientInfoManager clientInfoManager;
	@Autowired
	private ServerRegistry serverRegistry;

	private ObjectMapper normalJackson = JsonUtils.jackson();
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
		BaseMessage request = deserializer(data);
		BaseMessage response = client(request);
		if (request.getClass().isAnnotationPresent(NoLogMessage.class) == false
				&& response.getClass().isAnnotationPresent(NoLogMessage.class) == false) {
			logger.info("{}\n{}\n{}", logSummary(request, response), logMessage(request), logMessage(response));
		}
		return serializer(response);
	}

	@PostMapping(value = "worker", consumes = TEXT_PLAIN_VALUE, produces = TEXT_PLAIN_VALUE)
	public String worker(@RequestBody String data) throws Exception {
		BaseMessage request = deserializer(data);
		BaseMessage response = worker(request);
		if (request.getClass().isAnnotationPresent(NoLogMessage.class) == false
				&& response.getClass().isAnnotationPresent(NoLogMessage.class) == false) {
			logger.info("{}\n{}\n{}", logSummary(request, response), logMessage(request), logMessage(response));
		}
		return serializer(response);
	}

	public BaseMessage client(BaseMessage message) throws Exception {
		Class<? extends BaseMessage> type = message.getClass();
		if (type == RegistryMessage.class) {
			RegistryMessage registryMessage = (RegistryMessage) message;
			ClientInfo registryClient = registryMessage.getRegistryClient();
			clientInfoManager.registryClient(registryClient);
			registryMessage.setServers(serverRegistry.serverUrls());
			return message;
		}
		String clientId = message.getClientId();
		clientManager.update(clientId, message.getExchangeId());

		ClientSession clientSession = clientManager.sessionByCid(clientId);
		List<WorkerSession> workerSessions = null;
		if (CollectionUtils.isNotEmpty(message.getToWorkers())) {
			workerSessions = workerManager.sessions(message.getToWorkers());
		} else {
			workerSessions = workerManager.sessions(clientSession.getWorkerIds());
		}
		IClientMessageHandler handler = clientHandlers.get(type);
		if (handler == null) {
			if (CollectionUtils.isEmpty(workerSessions)) {
				return RejectMessage.sreason(message.getExchangeId(), "Current workers offline or session expired.");
			}
			for (WorkerSession workerSession : workerSessions) {
				workerManager.putMessage(workerSession, clientId, message);
			}
			return AsyncExecAckMessage.sack(message.getExchangeId());
		} else {
			if (CollectionUtils.isEmpty(workerSessions) && handler.needWorkerOnline()) {
				return RejectMessage.sreason(message.getExchangeId(), "Current workers offline or session expired.");
			}
			if (handler.asyncMode()) {
				if (CollectionUtils.isNotEmpty(workerSessions)) {
					for (WorkerSession workerSession : workerSessions) {
						ExchangeTask task = new ExchangeTask(handler, clientSession, workerSession, message,
								clientManager, workerManager);
						executorService.execute(task);
					}
				} else {
					ExchangeTask task = new ExchangeTask(handler, clientSession, null, message, clientManager,
							workerManager);
					executorService.execute(task);
				}
				return AsyncExecAckMessage.sack(message.getExchangeId());
			} else {
				BaseMessage handle = handler.handle(workerSessions, clientSession, message);
				clientManager.update(clientSession);
				workerManager.update(workerSessions);
				return handle;
			}
		}
	}

	public BaseMessage worker(BaseMessage message) throws Exception {
		Class<? extends BaseMessage> type = message.getClass();
		if (type == RegistryMessage.class) {
			RegistryMessage registryMessage = (RegistryMessage) message;
			ClientInfo registryClient = registryMessage.getRegistryClient();
			clientInfoManager.registryWorker(registryClient);
			registryMessage.setServers(serverRegistry.serverUrls());
			return message;
		}
		String clientId = message.getClientId();
		workerManager.update(clientId);

		WorkerSession workerSession = workerManager.session(clientId);
		ClientSession clientSession = clientManager.sessionByEid(message.getExchangeId());

		IWorkerMessageHandler handler = workerHandlers.get(type);
		if (handler == null) {
			if (clientSession == null) {
				return RejectMessage.sreason(message.getExchangeId(), "Current client offline or session expired.");
			}
			clientManager.putMessage(clientSession.getClientId(), message);
			return AckMessage.sack(message.getExchangeId());
		} else {
			BaseMessage handle = handler.handle(workerSession, clientSession, message);
			workerManager.update(workerSession);
			clientManager.update(clientSession);
			return handle;
		}

	}

	private BaseMessage deserializer(String data) throws Exception {
		return jackson.readValue(data, BaseMessage.class);
	}

	private String serializer(BaseMessage message) throws Exception {
		return jackson.writeValueAsString(message);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String logMessage(BaseMessage msg) throws JsonProcessingException {
		Map map = normalJackson.convertValue(msg, Map.class);
		map.remove("client");
		map.remove("exchangeId");
		map.remove("toWorkers");
		NoLogFields noLogFields = msg.getClass().getAnnotation(NoLogFields.class);
		if (noLogFields != null) {
			String[] values = noLogFields.values();
			for (String value : values) {
				if (map.containsKey(value)) {
					map.put(value, "Log Ignored");
				}
			}
		}
		return String.format("[%s] %s", msg.getClass().getSimpleName(), normalJackson.writeValueAsString(map));
	}

	private String logSummary(BaseMessage request, BaseMessage response) {
		if (request instanceof RegistryMessage) {
			return "Registry Message";
		}
		ClientInfo fromClient = clientInfoManager.findClientInfo(request.getClientId());
		ClientInfo toClient = clientInfoManager.findClientInfo(response.getClientId());
		return String.format("%s->%s eid %s", clientHost(fromClient), clientHost(toClient), request.getExchangeId());
	}

	private String clientHost(ClientInfo clientInfo) {
		if (clientInfo != null) {
			return clientInfo.getHost();
		}
		return "EXPIRED";
	}

}
