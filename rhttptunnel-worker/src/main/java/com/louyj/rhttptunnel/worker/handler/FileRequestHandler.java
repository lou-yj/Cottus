package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.model.http.Endpoints.WORKER_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.status.RejectReason.SERVER_BAD_RESPONSE;
import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.FileDataMessage;
import com.louyj.rhttptunnel.model.message.FileRequestMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class FileRequestHandler implements IMessageHandler {

	@Value("${work.directory}")
	private String workDirectory;

	@Autowired
	private MessageExchanger messageExchanger;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return FileRequestMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		FileRequestMessage fileRequestMessage = (FileRequestMessage) message;
		File file = null;
		if (fileRequestMessage.isAbsolute()) {
			file = new File(fileRequestMessage.getPath());
		} else {
			file = new File(workDirectory, fileRequestMessage.getPath());
		}
		long totalSize = file.length();
		long currentSize = 0;
		FileInputStream fis = new FileInputStream(file);
		String md5Hex = DigestUtils.md5Hex(fis);
		fis.close();
		fis = new FileInputStream(file);
		byte[] buffer = new byte[fileRequestMessage.getPartSize()];
		boolean start = true;
		boolean end = false;
		while (true) {
			int read = fis.read(buffer);
			byte[] data = buffer;
			if (read == -1) {
				data = new byte[0];
				end = true;
				fis.close();
			} else if (read != buffer.length) {
				data = new byte[read];
				System.arraycopy(buffer, 0, data, 0, read);
			}
			currentSize += read;
			FileDataMessage fileDataMessage = new FileDataMessage(CLIENT, file.getName(), start, end, data, md5Hex);
			fileDataMessage.setExchangeId(fileRequestMessage.getExchangeId());
			fileDataMessage.setSize(totalSize, currentSize);
			BaseMessage ackMessage = messageExchanger.jsonPost(WORKER_EXCHANGE, fileDataMessage);
			if ((ackMessage instanceof AckMessage) == false) {
				return Lists.newArrayList(
						RejectMessage.creason(CLIENT, message.getExchangeId(), SERVER_BAD_RESPONSE.reason()));
			}
			if (end) {
				return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
			}
			if (start) {
				start = false;
			}
		}
	}

}
