package com.louyj.rhttptunnel.worker.message;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;
import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.LongPullMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.worker.ClientDetector;
import com.louyj.rhttptunnel.worker.handler.IMessageHandler;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class MainLoop extends Thread implements ApplicationContextAware, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;

	@Autowired
	private MessageExchanger messageExchanger;

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
		this.start();
	}

	@Override
	public void run() {
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
		LongPullMessage longPullMessage = new LongPullMessage(ClientDetector.CLIENT);
		BaseMessage taskMessage = messageExchanger.jsonPost(WORKER_EXCHANGE, longPullMessage);
		Class<? extends BaseMessage> type = taskMessage.getClass();
		logger.info("Receive message {} content {}", type.getSimpleName(), taskMessage);
		IMessageHandler messageHandler = messageHandlers.get(type);
		if (messageHandler == null) {
			logger.error("Unknow Message Type {}", type);
			return;
		}
		List<BaseMessage> messages = null;
		try {
			messages = messageHandler.handle(taskMessage);
		} catch (Exception e) {
			String reason = "Exception " + e.getClass().getName() + ":" + e.getMessage();
			messages = Lists.newArrayList(RejectMessage.creason(CLIENT, taskMessage.getExchangeId(), reason));
		}
		if (messages != null) {
			for (BaseMessage msg : messages) {
				msg.setExchangeId(taskMessage.getExchangeId());
				messageExchanger.jsonPost(WORKER_EXCHANGE, msg);
			}
		}
		logger.info("Process finished.");
	}

}
