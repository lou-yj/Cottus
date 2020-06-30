package com.louyj.rhttptunnel.client.cmd.server;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.commands.Quit;

import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.ExitMessage;

/**
 *
 * Created on 2020年3月26日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ExitCommand extends BaseCommand implements Quit.Command {

	@ShellMethod(value = "Exit the shell.", key = { "quit", "exit" })
	@ShellMethodAvailability("notWorkerContext")
	public void quit() {
		if (!StringUtils.equals("unknow", messageExchanger.getServerAddress())) {
			ExitMessage message = new ExitMessage(ClientDetector.CLIENT);
			messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		}
		System.exit(0);
	}

}
