package com.louyj.cottus.client.cmd.worker;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_WORKER;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_WORKER_MGR;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.cmd.BaseCommand;
import com.louyj.cottus.client.consts.Status;
import com.louyj.cottus.client.util.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ClientInfo;
import com.louyj.rhttptunnel.model.message.DiscoverMessage;
import com.louyj.rhttptunnel.model.message.SelectWorkerMessage;
import com.louyj.rhttptunnel.model.message.ShowWorkerInfoMessage;
import com.louyj.rhttptunnel.model.message.ShutdownMessage;
import com.louyj.rhttptunnel.model.message.UnSelectWorkerMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
@ShellCommandGroup("Worker Manage Commands")
public class ControlCommand extends BaseCommand {

	@CommandGroups({ CORE_WORKER })
	@ShellMethod(value = "Discover workers", key = { "discover", "workers" })
	@ShellMethodAvailability("serverContext")
	public String discover(
			@ShellOption(value = { "-l", "--labels" }, help = "filter labels", defaultValue = "") String labelStr) {
		String[] labelKvs = labelStr.split("\\s*,\\s*");
		DiscoverMessage message = new DiscoverMessage(ClientDetector.CLIENT);
		for (String labelKv : labelKvs) {
			String[] label = labelKv.split("\\s*=\\s*");
			if (label.length == 2) {
				message.getLabels().put(label[0], label[1]);
			} else if (label.length == 1) {
				message.getNoLables().add(label[0]);
			} else {
				LogUtils.printMessage("bad format " + labelKv, System.out);
				return Status.FAILED;
			}
		}
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_WORKER })
	@ShellMethod(value = "Select worker")
	@ShellMethodAvailability("serverContext")
	public String select(@ShellOption(value = { "-i",
			"--index" }, defaultValue = "", help = "select by worker indexs, multiple indeies separated by commas, eg: 1,2") String indexStr,
			@ShellOption(value = { "-l", "--labels" }, help = "select by labels", defaultValue = "") String labelStr) {
		List<WorkerInfo> discoverWorkers = session.getDiscoverWorkers();
		List<ClientInfo> selectedWorkers = Lists.newArrayList();
		if (StringUtils.isNotBlank(indexStr)) {
			String[] splits = indexStr.split("\\s*,\\s*");
			for (String split : splits) {
				int index = NumberUtils.toInt(split);
				index = index - 1;
				if (index < 0 || index >= discoverWorkers.size()) {
					return "Bad index, you can refresh workers using discover command.";
				}
				ClientInfo worker = discoverWorkers.get(index).getClientInfo();
				selectedWorkers.add(worker);
			}
		} else if (StringUtils.isNotBlank(labelStr)) {
			Map<String, String> filterLables = Maps.newHashMap();
			Set<String> noLables = Sets.newHashSet();
			String[] labelKvs = labelStr.split("\\s*,\\s*");
			for (String labelKv : labelKvs) {
				String[] label = labelKv.split("\\s*=\\s*");
				if (label.length == 2) {
					filterLables.put(label[0], label[1]);
				} else if (label.length == 1) {
					noLables.add(label[0]);
				} else {
					LogUtils.printMessage("bad format " + labelKv, System.out);
					return Status.FAILED;
				}
			}
			for (WorkerInfo workerInfo : session.getDiscoverWorkers()) {
				if (labelMatches(workerInfo.getLabels(), filterLables, noLables)) {
					selectedWorkers.add(workerInfo.getClientInfo());
				}
			}
		}
		if (CollectionUtils.size(selectedWorkers) < 1) {
			return "No workers selected";
		}
		session.setSelectedWorkers(selectedWorkers);
		List<String> selectedWorkerIds = Lists.newArrayList();
		selectedWorkers.forEach(e -> selectedWorkerIds.add(e.identify()));
		SelectWorkerMessage message = new SelectWorkerMessage(ClientDetector.CLIENT, selectedWorkerIds);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String status = messagePoller.pollExchangeMessage(response);
		if (StringUtils.isBlank(status)) {
			session.setWorkerConnected(true);
		}
		return status;
	}

	@CommandGroups({ CORE_WORKER })
	@ShellMethod(value = "exit to selected worker")
	@ShellMethodAvailability("workerContext")
	public String unselect() {
		UnSelectWorkerMessage message = new UnSelectWorkerMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String status = messagePoller.pollExchangeMessage(response);
		session.setWorkerConnected(false);
		session.setSelectedWorkers(null);
		return status;
	}

	@CommandGroups({ CORE_WORKER_MGR })
	@SuppressWarnings("resource")
	@ShellMethod(value = "Shutdown remote worker!!!")
	@ShellMethodAvailability("workerContext")
	public String shutdown() {
		System.out.println("WARNNING REMOTE WORKER WILL BE SHUTDOWN!!!");
		System.out.print("Enter yes to continue(yes/no)?");
		Scanner sc = new Scanner(System.in);
		String line = sc.nextLine();
		line = StringUtils.trim(line);
		if (StringUtils.equalsIgnoreCase(line, "yes") == false) {
			return "CANCELED";
		}
		ShutdownMessage message = new ShutdownMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		String resp = messagePoller.pollExchangeMessage(response);
		return resp;
	}

	@CommandGroups({ CORE_WORKER })
	@ShellMethod(value = "show worker info")
	@ShellMethodAvailability("workerContext")
	public String workerinfo() {
		ShowWorkerInfoMessage message = new ShowWorkerInfoMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_WORKER })
	@ShellMethod(value = "show selected workers")
	@ShellMethodAvailability("workerContext")
	public String selected() {
		List<ClientInfo> selectedWorkers = session.getSelectedWorkers();
		for (ClientInfo ci : selectedWorkers) {
			LogUtils.printMessage(String.format("worker %s[%s] id %s", ci.getHost(), ci.getIp(), ci.identify()),
					System.out);
		}
		return null;
	}

	private boolean labelMatches(Map<String, String> labelRule, Map<String, String> matchLabels, Set<String> noLabels) {
		if (CollectionUtils.isNotEmpty(noLabels)) {
			for (String noLabel : noLabels) {
				if (labelRule.containsKey(noLabel)) {
					return false;
				}
			}
		}
		if (MapUtils.isNotEmpty(matchLabels)) {
			for (Entry<String, String> entry : matchLabels.entrySet()) {
				String value = labelRule.get(entry.getKey());
				if (StringUtils.equals(value, entry.getValue()) == false) {
					return false;
				}
			}
		}
		return true;
	}

}
