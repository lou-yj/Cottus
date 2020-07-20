package com.louyj.rhttptunnel.client.cmd.client;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import com.louyj.rhttptunnel.client.ClientDetector;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.client.util.LogUtils;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ClientCommand extends BaseCommand {

	@ShellMethod(value = "show client id")
	@ShellMethodAvailability("serverContext")
	public String cid() {
		LogUtils.printMessage(ClientDetector.CLIENT.identify(), System.out);
		return null;
	}

}
