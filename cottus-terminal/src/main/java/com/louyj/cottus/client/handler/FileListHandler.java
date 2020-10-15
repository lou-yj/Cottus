package com.louyj.cottus.client.handler;

import java.io.PrintStream;

import com.louyj.cottus.client.ClientSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.file.FileListMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class FileListHandler implements IMessageHandler {

	@Autowired
	private ClientSession clientSession;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return FileListMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		FileListMessage fileListMessage = (FileListMessage) message;
		clientSession.setCwd(fileListMessage.getPath());
		writer.println(StringUtils.join(fileListMessage.getFiles(), "\n"));
	}

}
