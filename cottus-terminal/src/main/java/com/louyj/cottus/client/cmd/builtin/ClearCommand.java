package com.louyj.cottus.client.cmd.builtin;

import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_ALLOW_ALL;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_SYSTEM;

import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.cmd.BaseCommand;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Clear;

/**
 *
 * Created on 2020年3月26日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ClearCommand extends BaseCommand implements Clear.Command {

	@Autowired
	@Lazy
	private Terminal terminal;

	@CommandGroups({ CORE_SYSTEM, CORE_ALLOW_ALL })
	@ShellMethod("Clear the shell screen.")
	public void clear() {
		terminal.puts(InfoCmp.Capability.clear_screen);
	}

}
