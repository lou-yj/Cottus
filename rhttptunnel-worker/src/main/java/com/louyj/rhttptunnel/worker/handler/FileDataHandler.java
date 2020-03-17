package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.model.message.status.RejectReason.ACCESS_FILE_FAILED;
import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.FileDataMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

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
	public List<BaseMessage> handle(BaseMessage message) throws IOException {
		FileDataMessage partFileMessage = (FileDataMessage) message;
		File file = new File(workDirectory, partFileMessage.getFileName());
		if (partFileMessage.isStart()) {
			File parentFile = file.getParentFile();
			if (parentFile.exists() == false) {
				parentFile.mkdirs();
			} else if (parentFile.isDirectory() == false) {
				return Lists.newArrayList(
						RejectMessage.creason(CLIENT, message.getExchangeId(), ACCESS_FILE_FAILED.reason()));
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
				return Lists.newArrayList(AckMessage.cack(CLIENT, partFileMessage.getExchangeId()));
			} else {
				return Lists.newArrayList(RejectMessage.creason(CLIENT, message.getExchangeId(), "md5 not matched"));
			}
		} else {
			return null;
		}
	}

}
