package com.louyj.rhttptunnel.client.worker;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.MessagePoller;
import com.louyj.rhttptunnel.client.Status;
import com.louyj.rhttptunnel.model.bean.RoleType;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.SelectWorkerMessage;
import com.louyj.rhttptunnel.model.message.ShellEndMessage;
import com.louyj.rhttptunnel.model.message.ShellMessage;
import com.louyj.rhttptunnel.model.message.ShellStartMessage;
import com.louyj.rhttptunnel.model.message.ShutdownMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */

@ShellComponent
public class ControlCommand {

	@Autowired
	private ClientSession session;

	@Autowired
	private MessagePoller messagePoller;

	@Autowired
	private MessageExchanger messageExchanger;

	@ShellMethod(value = "exit to selected worker")
	public String unselect() {
		SelectWorkerMessage message = new SelectWorkerMessage(CLIENT, session.getSelectedWorker());
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String status = messagePoller.pollExchangeMessage(response);
		if (StringUtils.equals(Status.OK, status)) {
			session.setWorkerConnected(false);
			session.setSelectedWorker(null);
		}
		return status;
	}

	public Availability unselectAvailability() {
		return session.workerCmdAvailability();
	}

	@SuppressWarnings("resource")
	@ShellMethod(value = "Shutdown remote worker!!!")
	public String shutdown() {
		System.out.println("WARNNING REMOTE WORKER WILL BE SHUTDOWN!!!");
		System.out.print("Enter yes to continue(yes/no)?");
		Scanner sc = new Scanner(System.in);
		String line = sc.nextLine();
		line = StringUtils.trim(line);
		if (StringUtils.equalsIgnoreCase(line, "yes") == false) {
			return "CANCELED";
		}
		ShutdownMessage message = new ShutdownMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String resp = messagePoller.pollExchangeMessage(response);
		return resp;
	}

	public Availability shutdownAvailability() {
		return session.workerCmdAvailability();
	}

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
			System.out.print("shell:> ");
		} else {
			System.out.println(resp);
			return Status.FAILED;
		}
		while (true) {
			line = sc.nextLine();
			if (StringUtils.equals(StringUtils.trim(line), "exit")) {
				ShellEndMessage shellEndMessage = new ShellEndMessage(CLIENT, exchangeId);
				BaseMessage endMessage = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellEndMessage);
				String echo = messagePoller.pollExchangeMessage(endMessage);
				System.out.print(echo);
				break;
			}
			ShellMessage shellMessage = new ShellMessage(CLIENT, exchangeId);
			shellMessage.setMessage(line);
			BaseMessage post = messageExchanger.jsonPost(CLIENT_EXCHANGE, shellMessage);
			String echo = messagePoller.pollExchangeMessage(post);
			System.out.print(echo);
			System.out.print("shell:> ");
		}
		return "\nExit interactive shell mode";
	}

	public Availability shellAvailability() {
		return session.workerCmdAvailability(RoleType.ADMIN);
	}

}
