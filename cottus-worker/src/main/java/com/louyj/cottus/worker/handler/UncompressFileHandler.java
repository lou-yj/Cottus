package com.louyj.cottus.worker.handler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.file.UncompressFileMessage;
import com.louyj.rhttptunnel.model.util.CompressUtils;
import com.louyj.cottus.worker.ClientDetector;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class UncompressFileHandler implements IMessageHandler, InitializingBean {

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
		return UncompressFileMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws IOException {
		UncompressFileMessage uncompressFileMessage = (UncompressFileMessage) message;
		String sourcePath = null;
		String targetPath = null;
		if (uncompressFileMessage.getSource().startsWith("/")) {
			sourcePath = workDirectory + uncompressFileMessage.getSource();
		} else {
			sourcePath = workDirectory + "/" + uncompressFileMessage.getSource();
		}
		if (uncompressFileMessage.getTarget().startsWith("/")) {
			targetPath = workDirectory + uncompressFileMessage.getTarget();
		} else {
			targetPath = workDirectory + "/" + uncompressFileMessage.getTarget();
		}
		switch (uncompressFileMessage.getType()) {
		case "zip":
			CompressUtils.unZipFile(new File(sourcePath), new File(targetPath));
			if (uncompressFileMessage.isDeleteSource()) {
				new File(sourcePath).delete();
			}
			return Arrays.asList(AckMessage.cack(ClientDetector.CLIENT, message.getExchangeId()));
		default:
			return Arrays.asList(RejectMessage.creason(ClientDetector.CLIENT, message.getExchangeId(),
					String.format("Compress type %s current not supported", uncompressFileMessage.getType())));
		}
	}

}
