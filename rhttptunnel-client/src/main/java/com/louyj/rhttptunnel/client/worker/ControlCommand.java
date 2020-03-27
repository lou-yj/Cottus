package com.louyj.rhttptunnel.client.worker;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.MessagePoller;
import com.louyj.rhttptunnel.client.consts.Status;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ConfigGetMessage;
import com.louyj.rhttptunnel.model.message.ConfigSetMessage;
import com.louyj.rhttptunnel.model.message.SelectWorkerMessage;
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

	@ShellMethod(value = "Get config from server or worker")
	public String configGet(@ShellOption(value = { "-k",
			"--key" }, help = "config key, show all keys if not set", defaultValue = "") String key) {
		ConfigGetMessage message = new ConfigGetMessage(CLIENT);
		message.setKey(key);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability configGetAvailability() {
		return session.workerCmdAvailability();
	}

	@ShellMethod(value = "Set config to server or worker")
	public String configSet(@ShellOption(value = { "-k", "--key" }, help = "config key") String key,
			@ShellOption(value = { "-v", "--value" }, help = "config value") String value) {
		ConfigSetMessage message = new ConfigSetMessage(CLIENT);
		message.setKey(key);
		message.setValue(value);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	public Availability configSetAvailability() {
		return session.workerCmdAvailability();
	}

}
