package com.louyj.rhttptunnel.client.cmd.worker;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.client.consts.Status;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.DiscoverMessage;
import com.louyj.rhttptunnel.model.message.SelectWorkerMessage;
import com.louyj.rhttptunnel.model.message.ShutdownMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
@ShellCommandGroup("Worker Manage Commands")
public class WorkerManageCommand extends BaseCommand {

	@ShellMethod(value = "Discover workers")
	@ShellMethodAvailability("serverContext")
	public String discover() {
		DiscoverMessage message = new DiscoverMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "Select worker")
	@ShellMethodAvailability("serverContext")
	public String select(@ShellOption(value = { "-i", "--index" }, help = "worker index") int index) {
		List<WorkerInfo> discoverWorkers = session.getDiscoverWorkers();
		index = index - 1;
		if (index < 0 || index >= discoverWorkers.size()) {
			return "Bad index, you can refresh workers using discover command.";
		}
		ClientInfo worker = discoverWorkers.get(index).getClientInfo();
		session.setSelectedWorker(worker);
		SelectWorkerMessage message = new SelectWorkerMessage(CLIENT, worker);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String status = messagePoller.pollExchangeMessage(response);
		if (StringUtils.equals(Status.OK, status)) {
			session.setWorkerConnected(true);
		}
		return status;
	}

	@ShellMethod(value = "exit to selected worker")
	@ShellMethodAvailability("workerContext")
	public String unselect() {
		SelectWorkerMessage message = new SelectWorkerMessage(CLIENT, session.getSelectedWorker());
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String status = messagePoller.pollExchangeMessage(response);
		session.setWorkerConnected(false);
		session.setSelectedWorker(null);
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

}
