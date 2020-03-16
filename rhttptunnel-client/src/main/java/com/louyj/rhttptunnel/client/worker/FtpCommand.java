package com.louyj.rhttptunnel.client.worker;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.MessagePoller;
import com.louyj.rhttptunnel.client.util.LogUtils;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.FileDataMessage;
import com.louyj.rhttptunnel.model.message.FileRequestMessage;
import com.louyj.rhttptunnel.model.message.PwdMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */

@ShellComponent
public class FtpCommand {

	@Autowired
	private ClientSession session;

	@Autowired
	private MessagePoller messagePoller;

	@Autowired
	private MessageExchanger messageExchanger;

	@Value("${transfer.data.maxsize:1048576}")
	private int transferMaxSize;

	@ShellMethod(value = "fetch file from worker")
	public String fileGet(@ShellOption(value = { "-f", "--file" }, help = "file path") String path,
			@ShellOption(value = { "-a",
					"--absolute" }, help = "absolute path?", defaultValue = "false") boolean absolute) {
		FileRequestMessage message = new FileRequestMessage(CLIENT, absolute, path);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability fileGetAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "send file to worker")
	public String fileSend(@ShellOption(value = { "-f", "--file" }, help = "file path") String path) throws Exception {
		String exchangeId = UUID.randomUUID().toString();
		File file = new File(path);
		if (!file.exists()) {
			LogUtils.clientError("file not exists");
			return "FAILED";
		}
		if (file.isDirectory()) {
			LogUtils.clientError("directory transfer current not support");
			return "FAILED";
		}
		long totalSize = file.length();
		long currentSize = 0;
		FileInputStream fis = new FileInputStream(file);
		String md5Hex = DigestUtils.md5Hex(fis);
		fis.close();
		fis = new FileInputStream(file);
		byte[] buffer = new byte[transferMaxSize];
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
			fileDataMessage.setExchangeId(exchangeId);
			fileDataMessage.setSize(totalSize, currentSize);
			BaseMessage responseMessage = messageExchanger.jsonPost(CLIENT_EXCHANGE, fileDataMessage);
			if (responseMessage instanceof RejectMessage) {
				LogUtils.serverReject(responseMessage);
				fis.close();
				return "FAILED";
			}
			if (end) {
				return messagePoller.pollExchangeMessage(exchangeId);
			}
			if (start) {
				start = false;
			}
		}
	}

	public Availability fileSendAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "print work directory")
	public String pwd() {
		PwdMessage message = new PwdMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability pwdGetAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "list files")
	public String ls() {
		FileRequestMessage message = new FileRequestMessage(CLIENT, absolute, path);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability lsGetAvailability() {
		return session.workerCmdAvailability();
	}
}
