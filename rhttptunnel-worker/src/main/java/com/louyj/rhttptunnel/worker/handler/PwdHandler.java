package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.file.PwdMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class PwdHandler implements IMessageHandler {

	@Value("${work.directory}")
	private String workDirectory;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return PwdMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()).withMessage(workDirectory));
	}

}
