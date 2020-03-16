package com.louyj.rhttptunnel.client.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.louyj.rhttptunnel.client.ClientSession;

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

	@ShellMethod(value = "Discover workers")
	public String discover() {
		return "workers";
	}

	public Availability discoverAvailability() {
		return session.serverCmdAvailability();
	}

}
