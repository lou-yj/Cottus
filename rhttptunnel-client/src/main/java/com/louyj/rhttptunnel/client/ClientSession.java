package com.louyj.rhttptunnel.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.Availability;
import org.springframework.stereotype.Component;

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

	private ClientInfo clientInfo;

	@Value("${java.io.tmpdir}")
	private String workDirectory;

	private String cwd;

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

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

}
