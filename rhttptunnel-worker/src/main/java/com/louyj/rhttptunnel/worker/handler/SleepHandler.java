package com.louyj.rhttptunnel.worker.handler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SleepMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class SleepHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return SleepMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) {
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
