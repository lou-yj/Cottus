package com.louyj.rhttptunnel.server.service.worker;

import static com.louyj.rhttptunnel.model.message.status.RejectReason.NOT_SUPPORT_OPERATION;

import java.util.Map;

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
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.server.handler.IMessageHandler;
import com.louyj.rhttptunnel.server.session.WorkerSession;
import com.louyj.rhttptunnel.server.session.WorkerSessionManager;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@RestController("worker")
public class WorkerExchangeService implements ApplicationContextAware, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;

	@Autowired
	private WorkerSessionManager workerManager;

	private Map<Class<? extends BaseMessage>, IMessageHandler> messageHandlers;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, IMessageHandler> beansOfType = applicationContext.getBeansOfType(IMessageHandler.class);
		messageHandlers = Maps.newHashMap();
		for (IMessageHandler messageHandler : beansOfType.values()) {
			Class<? extends BaseMessage> supportType = messageHandler.supportType();
			messageHandlers.put(supportType, messageHandler);
		}
	}

	@PostMapping("exchange")
	public BaseMessage exchange(BaseMessage message) throws Exception {
		Class<? extends BaseMessage> type = message.getClass();
		logger.info("Rective {} message, content {}", type.getSimpleName(), message);
		IMessageHandler handler = messageHandlers.get(type);
		if (handler == null) {
			return RejectMessage.sreason(message.getExchangeId(), NOT_SUPPORT_OPERATION);
		}
		ClientInfo client = message.getClient();
		workerManager.update(client);
		WorkerSession session = workerManager.session(client);
		return handler.handle(session, message);
	}

}
