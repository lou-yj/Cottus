package com.louyj.rhttptunnel.client.cmd.worker;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.client.consts.Status;
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
public class ControlCommand extends BaseCommand {

	@ShellMethod(value = "exit to selected worker")
	@ShellMethodAvailability("workerContext")
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

	@SuppressWarnings("resource")
	@ShellMethod(value = "Shutdown remote worker!!!")
	@ShellMethodAvailability("workerAdminContext")
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

	@ShellMethod(value = "Get config from server or worker")
	@ShellMethodAvailability("workerContext")
	public String configGet(@ShellOption(value = { "-k",
			"--key" }, help = "config key, show all keys if not set", defaultValue = "") String key) {
		ConfigGetMessage message = new ConfigGetMessage(CLIENT);
		message.setKey(key);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "Set config to server or worker")
	@ShellMethodAvailability("workerAdminContext")
	public String configSet(@ShellOption(value = { "-k", "--key" }, help = "config key") String key,
			@ShellOption(value = { "-v", "--value" }, help = "config value") String value) {
		ConfigSetMessage message = new ConfigSetMessage(CLIENT);
		message.setKey(key);
		message.setValue(value);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
