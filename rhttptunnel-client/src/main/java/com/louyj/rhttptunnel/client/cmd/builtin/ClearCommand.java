package com.louyj.rhttptunnel.client.cmd.builtin;

import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Clear;

import com.louyj.rhttptunnel.client.cmd.BaseCommand;

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

	@ShellMethod("Clear the shell screen.")
	public void clear() {
		terminal.puts(InfoCmp.Capability.clear_screen);
	}

}