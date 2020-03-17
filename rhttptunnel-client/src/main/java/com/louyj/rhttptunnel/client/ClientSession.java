package com.louyj.rhttptunnel.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.Availability;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ClientSession {

	private boolean serverConnected = false;

	private boolean workerConnected = false;

	private List<ClientInfo> discoverWorkers = Lists.newArrayList();
	private String discoverWorkerText;
	private ClientInfo selectedWorker;

	@Value("${java.io.tmpdir}")
	private String workDirectory;

	private String cwd;

	public ClientInfo getSelectedWorker() {
		return selectedWorker;
	}

	public void setSelectedWorker(ClientInfo selectedWorker) {
		this.selectedWorker = selectedWorker;
	}

	public String getDiscoverWorkerText() {
		return discoverWorkerText;
	}

	public void setDiscoverWorkerText(String discoverWorkerText) {
		String[] lines = discoverWorkerText.split("\n");
		List<ClientInfo> workers = Lists.newArrayList();
		for (int i = 1; i < lines.length - 1; i++) {
			String[] split = lines[i].split("\t");
			String host = split[1];
			String ip = split[2];
			workers.add(new ClientInfo(host, ip));
		}
		this.discoverWorkers = workers;
		this.discoverWorkerText = discoverWorkerText;
	}

	public List<ClientInfo> getDiscoverWorkers() {
		return discoverWorkers;
	}

	public void setDiscoverWorkers(List<ClientInfo> discoverWorkers) {
		this.discoverWorkers = discoverWorkers;
	}

	public String getCwd() {
		return cwd;
	}

	public void setCwd(String cwd) {
		this.cwd = cwd;
	}

	public String getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	public boolean inUnconnectedMode() {
		return !serverConnected;
	}

	public boolean inServerMode() {
		return serverConnected && !workerConnected;
	}

	public boolean inWorkerMode() {
		return serverConnected && workerConnected;
	}

	public Availability serverCmdAvailability() {
		if (inUnconnectedMode()) {
			return Availability.unavailable("Server command not available when unconnected mode.");
		}
		if (inWorkerMode()) {
			return Availability.unavailable("Server command not available when connected to worker");
		}
		return Availability.available();
	}

	public Availability workerCmdAvailability() {
		if (inUnconnectedMode()) {
			return Availability.unavailable("Worker command not available when unconnected mode.");
		}
		if (inServerMode()) {
			return Availability.unavailable("Worker command not available when not connected to worker");
		}
		return Availability.available();
	}

	public Availability clientCmdAvailability() {
		if (inWorkerMode()) {
			return Availability.unavailable("Client command not available when connected to worker.");
		}
		if (inServerMode()) {
			return Availability.unavailable("Client command not available when connect to server");
		}
		return Availability.available();
	}

	public boolean isWorkerConnected() {
		return workerConnected;
	}

	public void setWorkerConnected(boolean workerConnected) {
		this.workerConnected = workerConnected;
	}

	public boolean isServerConnected() {
		return serverConnected;
	}

	public void setServerConnected(boolean serverConnected) {
		this.serverConnected = serverConnected;
	}

}
