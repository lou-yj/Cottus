package com.louyj.rhttptunnel.client.cmd.server;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.client.consts.Status;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ConnectMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ServerCommand extends BaseCommand {

	@ShellMethod(value = "Connect to server")
	@ShellMethodAvailability("clientContext")
	public String connect(
			@ShellOption(value = { "-s",
					"--server" }, help = "server address", defaultValue = "http://localhost:18080") String address,
			@ShellOption(value = { "-u", "--user" }, help = "user name") String userName,
			@ShellOption(value = { "-p", "--password" }, help = "password") String password) {
		messageExchanger.setServerAddress(address);
		ConnectMessage connectMessage = new ConnectMessage(CLIENT);
		connectMessage.setUser(userName);
		connectMessage.setPassword(password);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, connectMessage);
		String resp = messagePoller.pollExchangeMessage(response);
		if (Status.OK.equals(resp)) {
			session.setServerConnected(true);
			ClientDetector.CLIENT.setUser(userName);
		}
		return resp;
	}

}
