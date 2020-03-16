package com.louyj.rhttptunnel.server.worker.handler;

import static com.louyj.rhttptunnel.model.message.status.RejectReason.ACCESS_FILE_FAILED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.FileDataMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.status.IRejectReason;
import com.louyj.rhttptunnel.server.worker.WorkerSession;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class FileDataHandler implements IMessageHandler, InitializingBean {

	@Value("${work.directory}")
	private String workDirectory;

	@Override
	public void afterPropertiesSet() throws Exception {
		File file = new File(workDirectory);
		if (file.exists() == false) {
			file.mkdirs();
		}
		if (file.exists() && file.isDirectory() == false) {
			throw new RuntimeException("File not a directory " + workDirectory);
		}
	}

	@Override
	public Class<? extends BaseMessage> supportType() {
		return FileDataMessage.class;
	}

	@Override
	public BaseMessage handle(WorkerSession session, BaseMessage message) throws IOException {
		FileDataMessage partFileMessage = (FileDataMessage) message;
		File file = new File(workDirectory, partFileMessage.getFileName());
		if (partFileMessage.isStart()) {
			File parentFile = file.getParentFile();
			if (parentFile.exists() == false) {
				parentFile.mkdirs();
			} else if (parentFile.isDirectory() == false) {
				return RejectMessage.sreason(message.getExchangeId(), ACCESS_FILE_FAILED);
			}
		}
		FileOutputStream fos = new FileOutputStream(file, true);
		IOUtils.write(partFileMessage.getData(), fos);
		fos.close();
		if (partFileMessage.isEnd()) {
			FileInputStream fis = new FileInputStream(file);
			String md5Hex = DigestUtils.md5Hex(fis);
			fis.close();
			if (StringUtils.equals(md5Hex, partFileMessage.getFileHash())) {
				return AckMessage.sack(partFileMessage.getExchangeId());
			} else {
				return RejectMessage.sreason(message.getExchangeId(), IRejectReason.make("md5 not matched"));
			}
		} else {
			return AckMessage.sack(partFileMessage.getExchangeId());
		}
	}

}
