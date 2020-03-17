package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.FileListMessage;
import com.louyj.rhttptunnel.model.message.LsMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class LsHandler implements IMessageHandler {

	@Value("${work.directory}")
	private String workDirectory;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return LsMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		LsMessage lsMessage = (LsMessage) message;
		String path = lsMessage.getPath();
		if (StringUtils.isBlank(path)) {
			path = workDirectory;
		}
		File file = new File(path);
		List<String> result = Lists.newArrayList();
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (File listFile : listFiles) {
				result.add((listFile.isDirectory() ? "D" : "F") + "\t" + listFile.getAbsolutePath());
			}
		} else {
			result.add("F\t" + file.getAbsolutePath());
		}
		FileListMessage fileListMessage = new FileListMessage(CLIENT);
		fileListMessage.setExchangeId(message.getExchangeId());
		fileListMessage.setPath(path);
		fileListMessage.setFiles(result);
		AckMessage ackMessage = AckMessage.cack(CLIENT, message.getExchangeId());
		return Lists.newArrayList(fileListMessage, ackMessage);
	}

}
