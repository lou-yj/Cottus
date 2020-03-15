package com.louyj.rhttptunnel.worker.message;

import static com.louyj.rhttptunnel.worker.ClientDetector.WORKER;
import static com.louyj.rhttptunnel.worker.message.Endpoints.EXCHANGE;

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

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.LongPullMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.status.IRejectReason;
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

	private void doRun() {
		LongPullMessage longPullMessage = new LongPullMessage(ClientDetector.WORKER);
		BaseMessage taskMessage = messageExchanger.jsonPost(EXCHANGE, longPullMessage);
		Class<? extends BaseMessage> type = taskMessage.getClass();
		logger.info("Receive message {} content {}", type.getSimpleName(), taskMessage);
		IMessageHandler messageHandler = messageHandlers.get(type);
		if (messageHandler == null) {
			logger.error("Unknow Message Type {}", type);
			return;
		}
		BaseMessage result = null;
		try {
			result = messageHandler.handle(taskMessage);
		} catch (Exception e) {
			String reason = "Exception " + e.getClass().getName() + ":" + e.getMessage();
			result = RejectMessage.creason(WORKER, taskMessage.getExchangeId(), IRejectReason.make(reason));
		}
		if (result != null) {
			result.setExchangeId(taskMessage.getExchangeId());
			messageExchanger.jsonPost(EXCHANGE, result);
		}
		logger.info("Process finished.");
	}

}
