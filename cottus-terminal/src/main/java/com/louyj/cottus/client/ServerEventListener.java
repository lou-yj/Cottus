package com.louyj.cottus.client;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.http.ExchangeContext;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ServerEventLongPullMessage;

/**
 *
 * Created on 2020年3月26日
 *
 * @author Louyj
 *
 */
@Component
public class ServerEventListener extends Thread implements InitializingBean {

	@Autowired
	private MessageExchanger messageExchanger;
	@Autowired
	private MessagePoller messagePoller;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setDaemon(true);
		this.start();
	}

	@Override
	public void run() {
		while (this.isInterrupted() == false) {
			if (ClientDetector.CLIENT.identify() == null) {
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
				}
				continue;
			} else {
				break;
			}
		}
		while (this.isInterrupted() == false) {
			try {
				if (messageExchanger.isServerConnected()) {
					ExchangeContext exchangeContext = new ExchangeContext();
					exchangeContext.setClientId(ClientDetector.CLIENT.identify());
					exchangeContext.setCommand("heartbeat");
					messageExchanger.getExchangeContext().set(exchangeContext);

					ServerEventLongPullMessage message = new ServerEventLongPullMessage(ClientDetector.CLIENT);
					BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
					String polled = messagePoller.pollExchangeMessage(response);
					if (StringUtils.isNotBlank(polled)) {
						try {
							TimeUnit.SECONDS.sleep(5);
						} catch (InterruptedException ex) {
						}
					}
				} else {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException ex) {
					}
				}
			} catch (Exception e) {
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException ex) {
				}
			}
		}
	}

}
