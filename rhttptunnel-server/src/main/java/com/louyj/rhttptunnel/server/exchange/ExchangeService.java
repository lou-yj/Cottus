package com.louyj.rhttptunnel.server.exchange;

import static com.louyj.rhttptunnel.model.message.consts.CustomHeaders.CLIENT_ID;
import static com.louyj.rhttptunnel.model.message.consts.CustomHeaders.ENCRYPT_TYPE;
import static com.louyj.rhttptunnel.model.message.consts.CustomHeaders.MESSAGE_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
import com.louyj.rhttptunnel.model.message.consts.CustomHeaders;
import com.louyj.rhttptunnel.model.message.consts.EncryptType;
import com.louyj.rhttptunnel.model.util.AESEncryptUtils;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.model.util.RsaUtils;
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

	@PostMapping(value = "client", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_OCTET_STREAM_VALUE)
	public byte[] client(@RequestBody byte[] data, @RequestHeader(MESSAGE_TYPE) String messageType,
			@RequestHeader(ENCRYPT_TYPE) String enctyptType, @RequestHeader(CLIENT_ID) String clientId,
			HttpServletResponse httpResponse) throws Exception {
		ClientSession clientSession = clientManager.sessionByCid(clientId);
		BaseMessage request = deserializer(data, messageType, enctyptType, clientSession.getAesKey());
		BaseMessage response = client(request);
		if (request.getClass().isAnnotationPresent(NoLogMessage.class) == false
				&& response.getClass().isAnnotationPresent(NoLogMessage.class) == false) {
			logger.info("{}\n{}\n{}", logSummary(request, response), logMessage(request), logMessage(response));
		}
		clientSession = clientManager.sessionByCid(clientId);
		return serializer(httpResponse, response, clientSession.getPublicKey(), clientSession.getAesKey());
	}

	@PostMapping(value = "worker", consumes = APPLICATION_OCTET_STREAM_VALUE, produces = APPLICATION_OCTET_STREAM_VALUE)
	public byte[] worker(@RequestBody byte[] data, @RequestHeader(MESSAGE_TYPE) String messageType,
			@RequestHeader(ENCRYPT_TYPE) String enctyptType, @RequestHeader(CLIENT_ID) String clientId,
			HttpServletResponse httpResponse) throws Exception {
		WorkerSession workerSession = workerManager.session(clientId);
		BaseMessage request = deserializer(data, messageType, enctyptType, workerSession.getAesKey());
		BaseMessage response = worker(request);
		if (request.getClass().isAnnotationPresent(NoLogMessage.class) == false
				&& response.getClass().isAnnotationPresent(NoLogMessage.class) == false) {
			logger.info("{}\n{}\n{}", logSummary(request, response), logMessage(request), logMessage(response));
		}
		workerSession = workerManager.session(clientId);
		return serializer(httpResponse, response, workerSession.getPublicKey(), workerSession.getAesKey());
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
		ClientSession clientSession = clientManager.update(clientId, message.getExchangeId());
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

		WorkerSession workerSession = workerManager.update(clientId);
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

	private BaseMessage deserializer(byte[] data, String messageType, String enctyptType, String aesKey)
			throws Exception {
		if (StringUtils.isNotBlank(enctyptType)) {
			switch (EncryptType.of(enctyptType)) {
			case RSA:
				data = RsaUtils.decrypt(data, serverRegistry.getPrivateKey());
				break;
			case AES:
				data = AESEncryptUtils.decrypt(data, aesKey);
				break;
			case NONE:
				break;
			default:
				break;
			}
		}
		return (BaseMessage) jackson.readValue(data, Class.forName(messageType));
	}

	private byte[] serializer(HttpServletResponse httpResponse, BaseMessage message, Key publicKey, String aesKey)
			throws Exception {
		httpResponse.setHeader(CustomHeaders.MESSAGE_TYPE, message.getClass().getName());
		String json = jackson.writeValueAsString(message);
		byte[] data = json.getBytes(StandardCharsets.UTF_8);
		EncryptType encryptType = EncryptType.NONE;
		if (aesKey != null) {
			encryptType = EncryptType.AES;
		} else if (publicKey != null) {
			encryptType = EncryptType.RSA;
		}
		switch (encryptType) {
		case RSA:
			data = RsaUtils.encrypt(data, publicKey);
			httpResponse.setHeader(CustomHeaders.ENCRYPT_TYPE, EncryptType.RSA.name());
			break;
		case AES:
			data = AESEncryptUtils.encrypt(data, aesKey);
			httpResponse.setHeader(CustomHeaders.ENCRYPT_TYPE, EncryptType.AES.name());
			break;
		default:
			httpResponse.setHeader(CustomHeaders.ENCRYPT_TYPE, EncryptType.NONE.name());
			break;
		}
		return data;
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
