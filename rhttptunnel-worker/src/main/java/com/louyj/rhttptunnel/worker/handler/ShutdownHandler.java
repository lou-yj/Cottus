package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.config.IConfigListener;
import com.louyj.rhttptunnel.model.http.Endpoints;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShutdownMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShutdownHandler implements IMessageHandler, IConfigListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String REMOTE_SHUTDOWN_ENABLE = "remote.shutdown.enable";

	@Autowired
	private MessageExchanger messageExchanger;

	private boolean remoteShutdownEnable = false;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShutdownMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		new Thread() {

			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e) {
					logger.error("", e);
				}
				ShutdownMessage shutdownMessage = new ShutdownMessage(CLIENT);
				messageExchanger.jsonPost(Endpoints.WORKER_EXCHANGE, shutdownMessage);
				logger.info("Shutdown VM");
				System.exit(0);
			}

		}.start();
		return Lists.newArrayList(
				AckMessage.cack(CLIENT, message.getExchangeId()).withMessage("Worker VM will exit in 10 seconds."));
	}

	@Override
	public List<String> keys() {
		return Lists.newArrayList(REMOTE_SHUTDOWN_ENABLE);
	}

	@Override
	public String value(String clientId, String key) {
		return String.valueOf(remoteShutdownEnable);
	}

	@Override
	public void onChanged(String clientId, String key, String value) {
		remoteShutdownEnable = BooleanUtils.toBoolean(value);
	}

}
