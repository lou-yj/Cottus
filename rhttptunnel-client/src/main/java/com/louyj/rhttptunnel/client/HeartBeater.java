package com.louyj.rhttptunnel.client;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.HeartBeatMessage;

/**
 *
 * Created on 2020年3月26日
 *
 * @author Louyj
 *
 */
@Component
public class HeartBeater extends Thread implements InitializingBean {

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setDaemon(true);
		this.start();
	}

	@Override
	public void run() {
		while (this.isInterrupted() == false) {

			try {
				if (!StringUtils.equals("unknow", messageExchanger.getServerAddress())) {
					HeartBeatMessage message = new HeartBeatMessage(ClientDetector.CLIENT);
					messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
				}
			} catch (Exception e) {
			}
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

}
