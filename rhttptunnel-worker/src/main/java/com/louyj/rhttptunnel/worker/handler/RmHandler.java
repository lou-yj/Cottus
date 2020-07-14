package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.file.RmMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class RmHandler implements IMessageHandler {

	@Value("${work.directory}")
	private String workDirectory;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RmMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws IOException {
		RmMessage rmMessage = (RmMessage) message;
		File file = null;
		if (rmMessage.isAbsolute()) {
			file = new File(rmMessage.getPath());
		} else {
			file = new File(workDirectory, rmMessage.getPath());
		}
		if (file.exists() == false) {
			return Lists.newArrayList(AckMessage.cack(CLIENT, rmMessage.getExchangeId()));
		}
		if (file.isDirectory() && rmMessage.isDirectory() == false) {
			return Lists.newArrayList(RejectMessage.creason(CLIENT, message.getExchangeId(), "File is directory"));
		}
		try {
			if (file.isFile()) {
				file.delete();
			} else {
				FileUtils.deleteDirectory(file);
			}
			return Lists.newArrayList(AckMessage.cack(CLIENT, rmMessage.getExchangeId()));
		} catch (Exception e) {
			return Lists.newArrayList(RejectMessage.creason(CLIENT, message.getExchangeId(),
					e.getClass().getSimpleName() + ": " + e.getMessage()));
		}

	}

}
