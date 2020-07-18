package com.louyj.rhttptunnel.client.cmd.server;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.client.cmd.worker.ControlCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ConnectMessage;
import com.louyj.rhttptunnel.model.message.ListServersMessage;
import com.louyj.rhttptunnel.model.message.RegistryMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ServerCommand extends BaseCommand {

	@Autowired
	private ControlCommand workerManageCommand;

	@ShellMethod(value = "Connect to server")
	@ShellMethodAvailability("clientContext")
	public String connect(
			@ShellOption(value = { "-s",
					"--server" }, help = "server address", defaultValue = "http://localhost:18080") String address,
			@ShellOption(value = { "-u", "--user" }, help = "user name") String userName,
			@ShellOption(value = { "-p", "--password" }, help = "password") String password) {
		messageExchanger.setServerAddress(address);
		{
			RegistryMessage registryMessage = new RegistryMessage(CLIENT);
			registryMessage.setRegistryClient(CLIENT);
			BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, registryMessage);
			String resp = messagePoller.pollExchangeMessage(response);
			if (StringUtils.isNotBlank(resp)) {
				return null;
			}
		}
		ConnectMessage connectMessage = new ConnectMessage(CLIENT);
		connectMessage.setUser(userName);
		connectMessage.setPassword(password);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, connectMessage);
		String resp = messagePoller.pollExchangeMessage(response);
		if (StringUtils.isBlank(resp)) {
			session.setServerConnected(true);
			resp = workerManageCommand.discover("");
		}
		return resp;
	}

	@ShellMethod(value = "list servers")
	@ShellMethodAvailability("serverContext")
	public String servers() {
		ListServersMessage listServersMessage = new ListServersMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, listServersMessage);
		return messagePoller.pollExchangeMessage(response);
	}

}
