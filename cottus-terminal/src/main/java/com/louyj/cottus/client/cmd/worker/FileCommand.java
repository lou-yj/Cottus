package com.louyj.cottus.client.cmd.worker;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_WORKERFS;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.cmd.BaseCommand;
import com.louyj.cottus.client.consts.Status;
import com.louyj.cottus.client.util.LogUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.file.ExecMessage;
import com.louyj.rhttptunnel.model.message.file.FileDataMessage;
import com.louyj.rhttptunnel.model.message.file.FileRequestMessage;
import com.louyj.rhttptunnel.model.message.file.LsMessage;
import com.louyj.rhttptunnel.model.message.file.PwdMessage;
import com.louyj.rhttptunnel.model.message.file.RmMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */

@ShellComponent
@ShellCommandGroup("Worker FileSystem Commands")
public class FileCommand extends BaseCommand {

	@Value("${transfer.data.maxsize:1048576}")
	private int transferMaxSize;

	@CommandGroups({ CORE_WORKERFS })
	@ShellMethod(value = "get file from worker")
	@ShellMethodAvailability("workerContext")
	public String get(@ShellOption(value = { "-f", "--file" }, help = "file path") String path, @ShellOption(value = {
			"-a", "--absolute" }, help = "absolute path?", defaultValue = "false") boolean absolute) {
		FileRequestMessage message = new FileRequestMessage(ClientDetector.CLIENT, absolute, path);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_WORKERFS })
	@ShellMethod(value = "send file to worker")
	@ShellMethodAvailability("workerContext")
	public String send(@ShellOption(value = { "-f", "-s", "--file", "--source" }, help = "file path") String path,
			@ShellOption(value = { "-t", "--target" }, help = "file path", defaultValue = "") String target)
			throws Exception {
		String exchangeId = UUID.randomUUID().toString();
		File file = new File(path);
		if (!file.exists()) {
			LogUtils.clientError("file not exists", System.out);
			return Status.FAILED;
		}
		if (file.isDirectory()) {
			LogUtils.clientError("directory transfer current not support", System.out);
			return Status.FAILED;
		}
		String targetName = isBlank(target) ? file.getName() : target;
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
				currentSize += read;
			} else {
				currentSize += read;
			}
			FileDataMessage fileDataMessage = new FileDataMessage(ClientDetector.CLIENT, targetName, start, end, data, md5Hex);
			fileDataMessage.setExchangeId(exchangeId);
			fileDataMessage.setSize(totalSize, currentSize);
			BaseMessage responseMessage = messageExchanger.jsonPost(CLIENT_EXCHANGE, fileDataMessage);
			if (responseMessage instanceof RejectMessage) {
				LogUtils.serverReject(responseMessage, System.out);
				fis.close();
				return Status.FAILED;
			} else {
				System.out.println("Send package success, total size " + fileDataMessage.getTotalSize()
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

	@CommandGroups({ CORE_WORKERFS })
	@ShellMethod(value = "remove files")
	@ShellMethodAvailability("workerContext")
	public String rm(@ShellOption(value = { "-p", "--path" }, help = "file path") String path,
			@ShellOption(value = { "-a",
					"--absolute" }, help = "absolute path?", defaultValue = "false") boolean absolute,
			@ShellOption(value = { "-d",
					"--directory" }, help = "remove directory?", defaultValue = "false") boolean directory) {
		RmMessage message = new RmMessage(ClientDetector.CLIENT);
		message.setPath(path);
		message.setAbsolute(absolute);
		message.setDirectory(directory);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_WORKERFS })
	@ShellMethod(value = "print work directory")
	@ShellMethodAvailability("workerContext")
	public String pwd() {
		if (session.getCwd() != null) {
			return session.getCwd();
		}
		PwdMessage message = new PwdMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String pwd = messagePoller.pollExchangeMessage(response);
		session.setCwd(pwd);
		return pwd;
	}

	@CommandGroups({ CORE_WORKERFS })
	@ShellMethod(value = "list files")
	@ShellMethodAvailability("workerContext")
	public String ls(@ShellOption(value = { "-f", "--file" }, help = "file path", defaultValue = "") String path) {
		LsMessage message = new LsMessage(ClientDetector.CLIENT);
		message.setPath(isBlank(path) ? session.getCwd() : path);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_WORKERFS })
	@ShellMethod(value = "change directory")
	@ShellMethodAvailability("workerContext")
	public String cd(@ShellOption(value = { "-d", "--directory" }, help = "change to directory") String path) {
		pwd();
		if (path.startsWith("/")) {
			session.setCwd(path);
		} else {
			session.setCwd(session.getCwd() + "/" + path);
		}
		return session.getCwd();
	}

	@CommandGroups({ CORE_WORKERFS })
	@ShellMethod(value = "execute script file")
	@ShellMethodAvailability("workerContext")
	public String exec(@ShellOption(value = { "-f", "--file" }, help = "file path") String path, @ShellOption(value = {
			"-p", "--args",
			"--parameters" }, help = "parameters, when '-' input parameters in seperate line", defaultValue = "") String args,
			@ShellOption(value = { "-r", "--repeat" }, help = "repeat number", defaultValue = "1") int repeat)
			throws IOException {
		if (StringUtils.equals(args, "-")) {
			Terminal terminal = TerminalBuilder.builder().system(true).build();
			LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
			String prompt = "parameters:> ";
			args = lineReader.readLine(prompt);
			terminal.close();
		}
		if (repeat == -1) {
			repeat = Integer.MAX_VALUE;
		} else if (repeat <= 0) {
			repeat = 1;
		}
		ExecMessage message = new ExecMessage(ClientDetector.CLIENT, path, args);
		message.setWorkdir(session.getCwd());
		int num = 0;
		while (num < repeat) {
			BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
			String echo = messagePoller.pollExchangeMessage(response);
			LogUtils.printMessage(echo, System.out);
			num++;
		}
		return null;
	}

}
