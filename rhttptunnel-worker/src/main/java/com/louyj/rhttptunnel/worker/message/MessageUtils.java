package com.louyj.rhttptunnel.worker.message;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;
import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.annotation.NoLogMessage;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.IWorkerParallelMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.worker.handler.IMessageHandler;

/**
 *
 * Created on 2020年3月23日
 *
 * @author Louyj
 *
 */
@Component
public class MessageUtils implements ApplicationContextAware, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(MessageUtils.class);

	private ApplicationContext applicationContext;
	private static MessageExchanger messageExchanger;
	private static Map<Class<? extends BaseMessage>, IMessageHandler> messageHandlers;

	private static ExecutorService executorService = new ThreadPoolExecutor(10, 500, 300L, SECONDS,
			new SynchronousQueue<Runnable>());

	public static Map<Class<? extends BaseMessage>, IMessageHandler> getMessageHandlers() {
		return messageHandlers;
	}

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

	@Autowired
	public void setMessageExchanger(MessageExchanger messageExchanger) {
		MessageUtils.messageExchanger = messageExchanger;
	}

	public static void handle(BaseMessage taskMessage) {
		Class<? extends BaseMessage> type = taskMessage.getClass();
		boolean noLog = type.isAnnotationPresent(NoLogMessage.class);
		if (!noLog) {
			logger.info("[{}] Receive message {}", taskMessage.getExchangeId(), type.getSimpleName());
		}

		IMessageHandler messageHandler = messageHandlers.get(type);
		if (messageHandler == null) {
			logger.error("Unknow Message Type {}", type);
			return;
		}
		ParrallelTask parrallelTask = new ParrallelTask(messageHandler, taskMessage, noLog);
		if (taskMessage instanceof IWorkerParallelMessage) {
			executorService.submit(parrallelTask);
		} else {
			parrallelTask.run();
		}
	}

	static class ParrallelTask implements Runnable {

		private IMessageHandler messageHandler;
		private BaseMessage taskMessage;
		private boolean noLog;

		public ParrallelTask(IMessageHandler messageHandler, BaseMessage taskMessage, boolean noLog) {
			super();
			this.messageHandler = messageHandler;
			this.taskMessage = taskMessage;
			this.noLog = noLog;
		}

		@Override
		public void run() {
			try {
				doRun();
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		private void doRun() {
			List<BaseMessage> messages = null;
			try {
				messages = messageHandler.handle(taskMessage);
			} catch (Exception e) {
				logger.error("", e);
				String reason = "Exception " + e.getClass().getName() + ":" + e.getMessage();
				messages = Lists.newArrayList(RejectMessage.creason(CLIENT, taskMessage.getExchangeId(), reason));
			}
			if (messages != null) {
				for (BaseMessage msg : messages) {
					msg.setExchangeId(taskMessage.getExchangeId());
					messageExchanger.jsonPost(WORKER_EXCHANGE, msg);
				}
			}
			if (!noLog)
				logger.info("[{}] Process finished.", taskMessage.getExchangeId());
		}

	}

}
