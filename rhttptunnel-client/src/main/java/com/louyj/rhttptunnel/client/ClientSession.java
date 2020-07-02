package com.louyj.rhttptunnel.client;

import static com.louyj.rhttptunnel.model.bean.RoleType.NORMAL;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.Availability;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.RoleType;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
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

	private List<WorkerInfo> discoverWorkers = Lists.newArrayList();
	private String discoverWorkerText;
	private List<ClientInfo> selectedWorkers;
	private RoleType role = RoleType.NORMAL;

	@Value("${java.io.tmpdir}")
	private String workDirectory;

	private String cwd = "/";

	public List<ClientInfo> getSelectedWorkers() {
		return selectedWorkers;
	}

	public void setSelectedWorkers(List<ClientInfo> selectedWorkers) {
		this.selectedWorkers = selectedWorkers;
	}

	public String getDiscoverWorkerText() {
		return discoverWorkerText;
	}

	public RoleType getRole() {
		return role;
	}

	public void setRole(RoleType role) {
		this.role = role;
	}

	public List<WorkerInfo> getDiscoverWorkers() {
		return discoverWorkers;
	}

	public void setDiscoverWorkers(List<WorkerInfo> discoverWorkers) {
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
		return serverCmdAvailability(NORMAL);
	}

	public Availability serverCmdAvailability(RoleType roleType) {
		if (role.getLevel() < roleType.getLevel()) {
			return Availability.unavailable("Permission deny.");
		}
		if (inUnconnectedMode()) {
			return Availability.unavailable("Current command not available when unconnected mode.");
		}
		if (inWorkerMode()) {
			return Availability.unavailable("Current command not available when connected to worker");
		}
		return Availability.available();
	}

	public Availability notWorkerCmdAvailability() {
		return notWorkerCmdAvailability(NORMAL);
	}

	public Availability notWorkerCmdAvailability(RoleType roleType) {
		if (role.getLevel() < roleType.getLevel()) {
			return Availability.unavailable("Permission deny.");
		}
		if (inWorkerMode()) {
			return Availability.unavailable("Current command not available when connected to worker");
		}
		return Availability.available();
	}

	public Availability workerCmdAvailability() {
		return workerCmdAvailability(NORMAL);
	}

	public Availability workerCmdAvailability(RoleType roleType) {
		if (role.getLevel() < roleType.getLevel()) {
			return Availability.unavailable("Permission deny.");
		}
		if (inUnconnectedMode()) {
			return Availability.unavailable("Current command not available when unconnected mode.");
		}
		if (inServerMode()) {
			return Availability.unavailable("Current command not available when not connected to worker");
		}
		return Availability.available();
	}

	public Availability clientCmdAvailability() {
		return clientCmdAvailability(RoleType.NORMAL);
	}

	public Availability clientCmdAvailability(RoleType roleType) {
		if (role.getLevel() < roleType.getLevel()) {
			return Availability.unavailable("Permission deny.");
		}
		if (inWorkerMode()) {
			return Availability.unavailable("Current command not available when connected to worker.");
		}
		if (inServerMode()) {
			return Availability.unavailable("Current command not available when connect to server");
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
