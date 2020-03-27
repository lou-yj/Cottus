package com.louyj.rhttptunnel.client.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.jline.reader.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.FileInputProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.commands.Script;

/**
 *
 * Created on 2020年3月26日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class ScriptCommand implements Script.Command {

	@Autowired
	private Shell shell;
	@Autowired
	private Parser parser;

	@ShellMethod(value = "Read and execute commands from a file.")
	public void script(File file) throws IOException {
		Reader reader = new FileReader(file);
		try (FileInputProvider inputProvider = new FileInputProvider(reader, parser)) {
			shell.run(inputProvider);
		}
	}

}
