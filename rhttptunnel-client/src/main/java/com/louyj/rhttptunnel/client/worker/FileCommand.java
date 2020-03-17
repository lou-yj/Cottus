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
import com.louyj.rhttptunnel.model.message.ExecMessage;
import com.louyj.rhttptunnel.model.message.FileDataMessage;
import com.louyj.rhttptunnel.model.message.FileRequestMessage;
import com.louyj.rhttptunnel.model.message.LsMessage;
import com.louyj.rhttptunnel.model.message.PwdMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.RmMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */

@ShellComponent
public class FileCommand {

	@Autowired
	private ClientSession session;

	@Autowired
	private MessagePoller messagePoller;

	@Autowired
	private MessageExchanger messageExchanger;

	@Value("${transfer.data.maxsize:1048576}")
	private int transferMaxSize;

	@ShellMethod(value = "get file from worker")
	public String get(@ShellOption(value = { "-f", "--file" }, help = "file path") String path, @ShellOption(value = {
			"-a", "--absolute" }, help = "absolute path?", defaultValue = "false") boolean absolute) {
		FileRequestMessage message = new FileRequestMessage(CLIENT, absolute, path);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability getAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "send file to worker")
	public String send(@ShellOption(value = { "-f", "--file" }, help = "file path") String path) throws Exception {
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
			} else {
				System.out.println("Semd package success, total size " + fileDataMessage.getTotalSize()
						+ " current received " + fileDataMessage.getCurrentSize());
			}
			if (end) {
				return messagePoller.pollExchangeMessage(exchangeId);
			}
			if (start) {
				start = false;
			}
		}
	}

	public Availability sendAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "remove files")
	public String rm(@ShellOption(value = { "-p", "--path" }, help = "file path") String path,
			@ShellOption(value = { "-a",
					"--absolute" }, help = "absolute path?", defaultValue = "false") boolean absolute,
			@ShellOption(value = { "-d",
					"--directory" }, help = "remove directory?", defaultValue = "false") boolean directory) {
		RmMessage message = new RmMessage(CLIENT);
		message.setPath(path);
		message.setAbsolute(absolute);
		message.setDirectory(directory);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability rmAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "print work directory")
	public String pwd() {
		if (session.getCwd() != null) {
			return session.getCwd();
		}
		PwdMessage message = new PwdMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String pwd = messagePoller.pollExchangeMessage(response);
		session.setCwd(pwd);
		return pwd;
	}

	public Availability pwdAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "list files")
	public String ls() {
		LsMessage message = new LsMessage(CLIENT);
		message.setPath(session.getCwd());
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability lsAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "change directory")
	public String cd(@ShellOption(value = { "-d", "--directory" }, help = "change to directory") String path) {
		pwd();
		if (path.startsWith("/")) {
			session.setCwd(path);
		} else {
			session.setCwd(session.getCwd() + "/" + path);
		}
		return session.getCwd();
	}

	public Availability cdAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "execute script file")
	public String exec(@ShellOption(value = { "-f", "--file" }, help = "file path") String path,
			@ShellOption(value = { "-t", "--timeout" }, help = "timeout seconds", defaultValue = "120") int timeout,
			@ShellOption(value = { "-p", "--args" }, help = "parameters") String args) {
		if (path.startsWith("/") == false) {
			path = session.getCwd() + "/" + path;
		}
		ExecMessage message = new ExecMessage(CLIENT, path, args, timeout);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability execAvailability() {
		return session.workerCmdAvailability();
	}
}
