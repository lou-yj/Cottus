package com.louyj.rhttptunnel.client;

import static com.louyj.rhttptunnel.model.bean.RoleType.NORMAL;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.Availability;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.Permission;
import com.louyj.rhttptunnel.model.bean.RoleType;
import com.louyj.rhttptunnel.model.bean.WorkerInfo;
import com.louyj.rhttptunnel.model.http.ExchangeContext;
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
	private Permission permission;

	@Value("${java.io.tmpdir}")
	private String workDirectory;

	private String cwd = "/";

	private boolean debug = true;

	private boolean superAdmin;

	public boolean isSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

	private ExchangeContext exchangeContext;

	public ExchangeContext getExchangeContext() {
		return exchangeContext;
	}

	public void setExchangeContext(ExchangeContext exchangeContext) {
		this.exchangeContext = exchangeContext;
	}

	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public boolean hasPermission(String command) {
		if (permission == null) {
			return false;
		}
		return permission.getCommands().contains(command);
	}

	public ClientInfo getSelectedWorker(String clientId) {
		for (ClientInfo clientInfo : selectedWorkers) {
			if (StringUtils.equals(clientInfo.identify(), clientId)) {
				return clientInfo;
			}
		}
		return null;
	}

	public void removeSelectedWorker(String wid) {
		List<ClientInfo> selectedWorkers = Lists.newArrayList();
		for (ClientInfo ci : this.selectedWorkers) {
			if (StringUtils.equals(ci.identify(), wid)) {
				continue;
			}
			selectedWorkers.add(ci);
		}
		this.selectedWorkers = selectedWorkers;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setDiscoverWorkerText(String discoverWorkerText) {
		this.discoverWorkerText = discoverWorkerText;
	}

	public List<ClientInfo> getSelectedWorkers() {
		return selectedWorkers;
	}

	public void setSelectedWorkers(List<ClientInfo> selectedWorkers) {
		this.selectedWorkers = selectedWorkers;
	}

	public String getDiscoverWorkerText() {
		return discoverWorkerText;
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
		if (inWorkerMode()) {
			return Availability.unavailable("Current command not available when connected to worker");
		}
		return Availability.available();
	}

	public Availability workerCmdAvailability() {
		return workerCmdAvailability(NORMAL);
	}

	public Availability workerCmdAvailability(RoleType roleType) {
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
