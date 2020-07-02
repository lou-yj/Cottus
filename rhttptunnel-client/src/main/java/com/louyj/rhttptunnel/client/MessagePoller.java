package com.louyj.rhttptunnel.client;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.client.consts.Status.FAILED;
import static com.louyj.rhttptunnel.client.consts.Status.OK;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.client.handler.IMessageHandler;
import com.louyj.rhttptunnel.client.util.LogUtils;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.AsyncExecAckMessage;
import com.louyj.rhttptunnel.model.message.AsyncFetchMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class MessagePoller implements ApplicationContextAware, InitializingBean {

	private ApplicationContext applicationContext;

	@Autowired
	private MessageExchanger messageExchanger;

	@Autowired
	private ClientSession session;

	@Value("${client.max.wait:600}")
	private int maxWait = 600;

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

	public String pollExchangeMessage(BaseMessage response) {
		try {
			if (response == null) {
				LogUtils.clientError("Null Point Exception");
				return FAILED;
			}
			if (response instanceof AsyncExecAckMessage) {
				return pollExchangeMessage(response.getExchangeId());
			}
			{
				IMessageHandler handler = messageHandlers.get(response.getClass());
				if (handler == null) {
					LogUtils.clientError("No message handler for type " + response.getClass().getSimpleName());
					return FAILED;
				}
				try {
					handler.handle(response);
					return OK;
				} catch (EndOfMessageException e) {
					return e.getMessage();
				}
			}
		} catch (Exception e) {
			LogUtils.clientError("[" + e.getClass().getName() + "]" + e.getMessage());
			return FAILED;
		}
	}

	public String pollExchangeMessage(String exchangeId) throws Exception {
		List<ClientInfo> selectedWorkers = session.getSelectedWorkers();
		if (selectedWorkers.size() == 1) {
			return pollExchangeMessageOnce(exchangeId);
		}
		for (int i = 0; i < selectedWorkers.size(); i++) {
			String messageOnce = pollExchangeMessageOnce(exchangeId);
			LogUtils.printMessage(messageOnce);
		}
		return null;
	}

	private String pollExchangeMessageOnce(String exchangeId) throws Exception {
		long start = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - start > maxWait * 1000) {
				LogUtils.clientError("Wait Timeout wait over " + maxWait + " second");
				return FAILED;
			}
			AsyncFetchMessage fetchMessage = new AsyncFetchMessage(CLIENT);
			fetchMessage.setExchangeId(exchangeId);
			BaseMessage respMsg = messageExchanger.jsonPost(CLIENT_EXCHANGE, fetchMessage);
			if (StringUtils.equals(respMsg.getExchangeId(), exchangeId) == false) {
				LogUtils.serverError("bad response, exchange id not matched");
				return FAILED;
			}
			LogUtils.printMessage("==========Worker " + respMsg.getClient().getHost() + "==========");
			IMessageHandler handler = messageHandlers.get(respMsg.getClass());
			if (handler == null) {
				LogUtils.clientError("No message handler for type " + respMsg.getClass().getSimpleName());
				return FAILED;
			}
			try {
				handler.handle(respMsg);
			} catch (EndOfMessageException e) {
				return e.getMessage();
			}
		}
	}

}
