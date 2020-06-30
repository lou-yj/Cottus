package com.louyj.rhttptunnel.client.extend;

import org.jline.reader.LineReader;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.utils.AttributedString;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.jline.PromptProvider;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
@SuppressWarnings("unused")
//@Order(InteractiveShellApplicationRunner.PRECEDENCE - 50)
public class CustomShellApplicationRunner implements ApplicationRunner {

	private final LineReader lineReader;

	private final PromptProvider promptProvider;

	private final Parser parser;

	private final CustomShell shell;

	private final Environment environment;

	public CustomShellApplicationRunner(LineReader lineReader, PromptProvider promptProvider, Parser parser,
			CustomShell shell, Environment environment) {
		this.lineReader = lineReader;
		this.promptProvider = promptProvider;
		this.parser = parser;
		this.shell = shell;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		InputProvider inputProvider = new JLineInputProvider(lineReader, promptProvider);
		shell.run(inputProvider);
	}

	public static class JLineInputProvider implements InputProvider {

		private final LineReader lineReader;

		private final PromptProvider promptProvider;

		public JLineInputProvider(LineReader lineReader, PromptProvider promptProvider) {
			this.lineReader = lineReader;
			this.promptProvider = promptProvider;
		}

		@Override
		public Input readInput() {
			try {
				AttributedString prompt = promptProvider.getPrompt();
				lineReader.readLine(prompt.toAnsi(lineReader.getTerminal()));
			} catch (UserInterruptException e) {
				if (e.getPartialLine().isEmpty()) {
					throw new ExitRequest(1);
				} else {
					return Input.EMPTY;
				}
			}
			return new ParsedLineInput(lineReader.getParsedLine());
		}
	}

}
