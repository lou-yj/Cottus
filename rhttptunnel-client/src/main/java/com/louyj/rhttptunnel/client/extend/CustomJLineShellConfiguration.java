package com.louyj.rhttptunnel.client.extend;

import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;

/**
 *
 * Create at 2020年6月30日
 *
 * @author Louyj
 *
 */
@Configuration
public class CustomJLineShellConfiguration extends JLineShellAutoConfiguration {

	@Bean(destroyMethod = "close")
	@Override
	public Terminal terminal() {
		try {
			return TerminalBuilder.builder().nativeSignals(true).signalHandler(Terminal.SignalHandler.SIG_IGN).build();
		} catch (IOException e) {
			throw new BeanCreationException("Could not create Terminal: " + e.getMessage());
		}
	}

}
