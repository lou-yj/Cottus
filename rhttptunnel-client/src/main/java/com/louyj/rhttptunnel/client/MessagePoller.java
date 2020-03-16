package com.louyj.rhttptunnel.client;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.client.Status.FAILED;
import static com.louyj.rhttptunnel.client.Status.OK;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.client.handler.IMessageHandler;
import com.louyj.rhttptunnel.client.util.LogUtils;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.AsyncExecAckMessage;
import com.louyj.rhttptunnel.model.message.AsyncFetchMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.NoContentMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

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

	@Value("${client.max.wait}")
	private int maxWait = 600;

	@Value("${client.noconent.wait}")
	private int noconentWait = 1;

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
			if (response instanceof RejectMessage) {
				LogUtils.serverReject(response);
				return FAILED;
			}
			if (response instanceof AckMessage) {
				return OK;
			}
			if (response instanceof AsyncExecAckMessage) {
				return pollExchangeMessage(response.getExchangeId());
			}
			LogUtils.serverError("UNKNOW MESSAGE: " + response.getClass().getSimpleName());
			return FAILED;
		} catch (Exception e) {
			LogUtils.clientError("[" + e.getClass().getName() + "]" + e.getMessage());
			return FAILED;
		}
	}

	public String pollExchangeMessage(String exchangeId) throws Exception {
		long start = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - start > maxWait * 1000) {
				LogUtils.clientError("Wait Timeout wait over " + maxWait + " second");
				return FAILED;
			}
			AsyncFetchMessage fetchMessage = new AsyncFetchMessage(CLIENT, 3);
			fetchMessage.setExchangeId(exchangeId);
			BaseMessage respMsg = messageExchanger.jsonPost(CLIENT_EXCHANGE, fetchMessage);
			if (StringUtils.equals(respMsg.getExchangeId(), exchangeId) == false) {
				LogUtils.serverError("bad response, exchange id not matched");
				continue;
			}
			if (respMsg instanceof AckMessage) {
				break;
			}
			if (respMsg instanceof RejectMessage) {
				LogUtils.serverReject(respMsg);
				return FAILED;
			}
			if (respMsg instanceof NoContentMessage) {
				TimeUnit.SECONDS.sleep(noconentWait);
				continue;
			}
			IMessageHandler handler = messageHandlers.get(respMsg.getClass());
			if (handler == null) {
				LogUtils.clientError("No message handler for type " + respMsg.getClass().getSimpleName());
				return FAILED;
			}
			handler.handle(respMsg);
		}
		return Status.OK;
	}

}
