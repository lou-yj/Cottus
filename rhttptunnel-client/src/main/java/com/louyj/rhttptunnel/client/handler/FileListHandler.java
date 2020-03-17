package com.louyj.rhttptunnel.client.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.FileListMessage;

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
	public void handle(BaseMessage message) throws Exception {
		FileListMessage fileListMessage = (FileListMessage) message;
		clientSession.setCwd(fileListMessage.getPath());
		System.out.println(StringUtils.join(fileListMessage.getFiles(), "\n"));
	}

}
