package com.louyj.rhttptunnel.worker.handler;

import java.util.concurrent.TimeUnit;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SleepMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
public class SleepHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return SleepMessage.class;
	}

	@Override
	public BaseMessage handle(BaseMessage message) {
		SleepMessage sleepMessage = (SleepMessage) message;
		if (sleepMessage.getSecond() <= 0) {
			return null;
		}
		try {
			TimeUnit.SECONDS.sleep(sleepMessage.getSecond());
		} catch (Exception e) {
		}
		return null;
	}

}
