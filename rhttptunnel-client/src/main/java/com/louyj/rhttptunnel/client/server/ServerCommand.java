package com.louyj.rhttptunnel.client.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.ClientSession;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ServerCommand {

	@Autowired
	private ClientSession session;

	@ShellMethod(value = "Connect to server")
	public String connect(@ShellOption(value = { "-s", "--server" }, help = "server address") String address,
			@ShellOption(value = { "-u", "--user" }, help = "user name") String userName,
			@ShellOption(value = { "-p", "--password" }, help = "password") String password) {
		session.setServerConnected(true);
		return "OK";
	}

	public Availability connectAvailability() {
		return session.clientCmdAvailability();
	}

}
