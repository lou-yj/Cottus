package com.louyj.rhttptunnel.client.server;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Quit;

import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.model.http.MessageExchanger;
import com.louyj.rhttptunnel.model.message.ExitMessage;

/**
 *
 * Created on 2020年3月26日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ExitCommand implements Quit.Command {

	@Autowired
	private MessageExchanger messageExchanger;

	@ShellMethod(value = "Exit the shell.", key = { "quit", "exit" })
	public void quit() {
		if (!StringUtils.equals("unknow", messageExchanger.getServerAddress())) {
			ExitMessage message = new ExitMessage(ClientDetector.CLIENT);
			messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		}
		System.exit(0);
	}

}