package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
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
public class ShutdownHandler implements IMessageHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShutdownMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		logger.info("Exchange acked by server, message {}", message);
		new Thread() {

			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e) {
					logger.error("", e);
				}
				logger.info("Shutdown VM");
				System.exit(0);
			}

		}.start();
		return Lists.newArrayList(
				AckMessage.cack(CLIENT, message.getExchangeId()).withMessage("Worker VM will exit in 10 seconds."));
	}

}
