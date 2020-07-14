package com.louyj.rhttptunnel.client.cmd.worker;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.config.ConfigGetMessage;
import com.louyj.rhttptunnel.model.message.config.ConfigSetMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */

@ShellComponent
@ShellCommandGroup("Worker Config Commands")
public class ConfigCommand extends BaseCommand {

	@ShellMethod(value = "Get config from server or worker")
	@ShellMethodAvailability("workerContext")
	public String configGet(@ShellOption(value = { "-k",
			"--key" }, help = "config key, show all keys if not set", defaultValue = "") String key) {
		ConfigGetMessage message = new ConfigGetMessage(CLIENT);
		message.setKey(key);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@ShellMethod(value = "Set config to server or worker")
	@ShellMethodAvailability("workerAdminContext")
	public String configSet(@ShellOption(value = { "-k", "--key" }, help = "config key") String key,
			@ShellOption(value = { "-v", "--value" }, help = "config value") String value) {
		ConfigSetMessage message = new ConfigSetMessage(CLIENT);
		message.setKey(key);
		message.setValue(value);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
