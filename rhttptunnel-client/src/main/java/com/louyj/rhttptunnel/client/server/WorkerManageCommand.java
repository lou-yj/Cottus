package com.louyj.rhttptunnel.client.server;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import java.util.List;

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
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.DiscoverMessage;
import com.louyj.rhttptunnel.model.message.SelectWorkerMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class WorkerManageCommand {

	@Autowired

	private ClientSession session;

	@Autowired
	private MessagePoller messagePoller;

	@Autowired
	private MessageExchanger messageExchanger;

	@ShellMethod(value = "Discover workers")
	public String discover() {
		DiscoverMessage message = new DiscoverMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String resp = messagePoller.pollExchangeMessage(response);
		session.setDiscoverWorkerText(resp);
		return resp;
	}

	public Availability discoverAvailability() {
		return session.serverCmdAvailability();
	}

	@ShellMethod(value = "Select worker")
	public String select(@ShellOption(value = { "-i", "--index" }, help = "worker index") int index) {
		List<ClientInfo> discoverWorkers = session.getDiscoverWorkers();
		index = index - 1;
		if (index < 0 || index >= discoverWorkers.size()) {
			return "Bad index, you can refresh workers using discover command.";
		}
		ClientInfo worker = discoverWorkers.get(index);
		session.setSelectedWorker(worker);
		SelectWorkerMessage message = new SelectWorkerMessage(CLIENT, worker);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String status = messagePoller.pollExchangeMessage(response);
		if (StringUtils.equals(Status.OK, status)) {
			session.setWorkerConnected(true);
		}
		return status;
	}

	public Availability selectAvailability() {
		return session.serverCmdAvailability();
	}

}
