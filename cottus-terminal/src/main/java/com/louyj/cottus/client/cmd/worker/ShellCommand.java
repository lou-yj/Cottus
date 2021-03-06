package com.louyj.cottus.client.cmd.worker;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_WORKER_MGR;

import java.util.Scanner;
import java.util.UUID;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.cmd.BaseCommand;
import com.louyj.cottus.client.cmd.ShellParser;
import com.louyj.cottus.client.consts.Status;
import com.louyj.cottus.client.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellEndMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellMessage;
import com.louyj.rhttptunnel.model.message.shell.ShellStartMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */

@ShellComponent
@ShellCommandGroup("Worker FileSystem Commands")
public class ShellCommand extends BaseCommand {

	@CommandGroups({ CORE_WORKER_MGR })
	@SuppressWarnings("resource")
	@ShellMethod(value = "Enter into interactive shell mode")
	@ShellMethodAvailability("workerContext")
	public String shell() throws Exception {
		System.out.println("OPERATION WILL NOT CONTROLED BY SERVER!!!");
		System.out.println("ANYTHING AT YOUR OWN RISK!!!");
		System.out.print("Enter yes to continue(yes/no)?");
		Scanner sc = new Scanner(System.in);
		String line = sc.nextLine();
		line = StringUtils.trim(line);
		if (StringUtils.equalsIgnoreCase(line, "yes") == false) {
			return "CANCELED";
		}
		System.out.println("Try to ask worker into interactive shell mode");
		String exchangeId = UUID.randomUUID().toString();
		ShellStartMessage shellStartMessage = new ShellStartMessage(ClientDetector.CLIENT, exchangeId);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellStartMessage);
		String resp = messagePoller.pollExchangeMessage(response);
		if (StringUtils.isBlank(resp)) {
			System.out.println("OK, Worker ready");
			System.out.println("WARNNING YOU NOW IN INTERACTIVE SHELL MODE!!!");
		} else {
			System.out.println(resp);
			return Status.FAILED;
		}
		String prompt = "shell:> ";
		ShellParser shellParser = new ShellParser();
		boolean isMultiLine = false;
		try {
			while (true) {
				if (isMultiLine) {
					isMultiLine = false;
				} else {
					System.out.print(prompt);
				}
				line = sc.nextLine();
				if (StringUtils.equals(StringUtils.trim(line), "exit")) {
					ShellEndMessage shellEndMessage = new ShellEndMessage(ClientDetector.CLIENT, exchangeId);
					BaseMessage endMessage = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellEndMessage);
					String echo = messagePoller.pollExchangeMessage(endMessage);
					LogUtils.printMessage(echo, System.out);
					break;
				}
				String parsedLine = shellParser.parse(line);
				if (parsedLine == null) {
					System.out.print("> ");
					isMultiLine = true;
					continue;
				}
				ShellMessage shellMessage = new ShellMessage(ClientDetector.CLIENT, exchangeId);
				shellMessage.setMessage(parsedLine);
				BaseMessage post = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellMessage);
				String echo = messagePoller.pollExchangeMessage(post);
				LogUtils.printMessage(echo, System.out);
			}
		} catch (UserInterruptException e) {
			LogUtils.printMessage("Try to ask worker work exit interactive shell mode", System.out);
			ShellEndMessage shellEndMessage = new ShellEndMessage(ClientDetector.CLIENT, exchangeId);
			BaseMessage endMessage = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellEndMessage);
			String echo = messagePoller.pollExchangeMessage(endMessage);
			LogUtils.printMessage(echo, System.out);
		} catch (EndOfFileException e) {
		}
		return "\nExit interactive shell mode";
	}

}
