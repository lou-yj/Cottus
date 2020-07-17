package com.louyj.rhttptunnel.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.util.StringUtils;

import com.louyj.rhttptunnel.model.bean.JsonFactory;
import com.louyj.rhttptunnel.model.http.MessageExchanger;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, JLineShellAutoConfiguration.class,
		SpringShellAutoConfiguration.class })
@Import({ MessageExchanger.class, JsonFactory.class })
public class RHttpTunnelClient {

	public static void main(String[] args) {
		// "--spring.shell.command.script.enabled=false",
		String[] disabledCommands = { "--spring.shell.command.stacktrace.enabled=false", };
		String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);
		SpringApplication.run(RHttpTunnelClient.class, fullArgs);
	}

}
