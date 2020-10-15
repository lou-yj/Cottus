package com.louyj.cottus.client.cmd.client;

import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_ALLOW_ALL;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_CLIENT;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.util.LogUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import com.louyj.cottus.client.cmd.BaseCommand;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ClientCommand extends BaseCommand {

	@CommandGroups({ CORE_CLIENT, CORE_ALLOW_ALL })
	@ShellMethod(value = "show client id")
	@ShellMethodAvailability("serverContext")
	public String cid() {
		LogUtils.printMessage(ClientDetector.CLIENT.identify(), System.out);
		return null;
	}

}
