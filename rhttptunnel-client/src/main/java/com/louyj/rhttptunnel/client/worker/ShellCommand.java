package com.louyj.rhttptunnel.client.worker;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.MessagePoller;
import com.louyj.rhttptunnel.client.consts.Status;
import com.louyj.rhttptunnel.model.bean.RoleType;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellEndMessage;
import com.louyj.rhttptunnel.model.message.ShellMessage;
import com.louyj.rhttptunnel.model.message.ShellStartMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */

@ShellComponent
public class ShellCommand {

	@Autowired
	private ClientSession session;

	@Autowired
	private MessagePoller messagePoller;

	@Autowired
	private MessageExchanger messageExchanger;

	@SuppressWarnings("resource")
	@ShellMethod(value = "Enter into interactive shell mode(Need Admin Privilege)")
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
		System.out.println("Try to ask worker work into interactive shell mode");
		String exchangeId = UUID.randomUUID().toString();
		ShellStartMessage shellStartMessage = new ShellStartMessage(CLIENT, exchangeId);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellStartMessage);
		String resp = messagePoller.pollExchangeMessage(response);
		if (StringUtils.equals(resp, "Worker ready")) {
			System.out.println("OK, Worker ready");
			System.out.println("WARNNING YOU NOW IN INTERACTIVE SHELL MODE!!!");
		} else {
			System.out.println(resp);
			return Status.FAILED;
		}
		Terminal terminal = TerminalBuilder.builder().system(true).build();
		LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
		String prompt = "shell:> ";
		try {
			while (true) {
				line = lineReader.readLine(prompt);
				if (StringUtils.equals(StringUtils.trim(line), "exit")) {
					ShellEndMessage shellEndMessage = new ShellEndMessage(CLIENT, exchangeId);
					BaseMessage endMessage = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellEndMessage);
					String echo = messagePoller.pollExchangeMessage(endMessage);
					System.out.print(echo);
					break;
				}
				if (StringUtils.endsWith(line, "\\")) {
					System.out.println("Multi line command current not support!");
				} else if (StringUtils.isNotBlank(line)) {
					ShellMessage shellMessage = new ShellMessage(CLIENT, exchangeId);
					shellMessage.setMessage(line);
					BaseMessage post = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellMessage);
					String echo = messagePoller.pollExchangeMessage(post);
					System.out.print(echo);
				}
			}
		} catch (UserInterruptException e) {
			System.out.println("Try to ask worker work exit interactive shell mode");
			ShellEndMessage shellEndMessage = new ShellEndMessage(CLIENT, exchangeId);
			BaseMessage endMessage = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellEndMessage);
			String echo = messagePoller.pollExchangeMessage(endMessage);
			System.out.print(echo);
		} catch (EndOfFileException e) {
		}
		IOUtils.closeQuietly(terminal);
		return "\nExit interactive shell mode";
	}

	public Availability shellAvailability() {
		return session.workerCmdAvailability(RoleType.ADMIN);
	}

}