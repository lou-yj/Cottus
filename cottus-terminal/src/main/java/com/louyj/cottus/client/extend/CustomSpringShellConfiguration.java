package com.louyj.cottus.client.extend;

import com.louyj.cottus.client.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.result.ResultHandlerConfig;

import com.louyj.rhttptunnel.model.http.MessageExchanger;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
@SuppressWarnings("rawtypes")
@Configuration
@Import(ResultHandlerConfig.class)
public class CustomSpringShellConfiguration extends SpringShellAutoConfiguration {

	@Autowired
	private ClientSession clientSession;
	@Autowired
	private MessageExchanger messageExchanger;

	@Bean
	public Shell shell(@Qualifier("main") ResultHandler resultHandler) {
		return new CustomShell(resultHandler, clientSession, messageExchanger);
	}

}
