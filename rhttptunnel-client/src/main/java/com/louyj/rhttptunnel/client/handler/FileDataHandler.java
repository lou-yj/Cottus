package com.louyj.rhttptunnel.client.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.file.FileDataMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class FileDataHandler implements IMessageHandler {

	@Autowired
	private ClientSession clientSession;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return FileDataMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		FileDataMessage fileDataMessage = (FileDataMessage) message;
		File file = new File(clientSession.getWorkDirectory(), fileDataMessage.getFileName());
		if (fileDataMessage.isStart()) {
			writer.println("Using " + clientSession.getWorkDirectory() + " as current work directory.");
			writer.println("Saving file at " + file.getAbsolutePath());
			File parentFile = file.getParentFile();
			if (parentFile.exists() == false) {
				parentFile.mkdirs();
			} else if (parentFile.isDirectory() == false) {
				writer.println("ERROR: cannot write file at " + file.getAbsolutePath());
				return;
			}
		}
		writer.println("Receive package, total size " + fileDataMessage.getTotalSize() + " current received "
				+ fileDataMessage.getCurrentSize());
		FileOutputStream fos = new FileOutputStream(file, true);
		IOUtils.write(fileDataMessage.getData(), fos);
		fos.close();
		if (fileDataMessage.isEnd()) {
			FileInputStream fis = new FileInputStream(file);
			String md5Hex = DigestUtils.md5Hex(fis);
			fis.close();
			if (StringUtils.equals(md5Hex, fileDataMessage.getFileHash())) {
				writer.println("File transfer finished, md5 hash " + md5Hex + " matched.");
			} else {
				writer.println("NETWORK ERROR: File transfer finished with wrong md5 hash.");
			}
		}
	}

}
