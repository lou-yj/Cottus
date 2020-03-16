package com.louyj.rhttptunnel.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.util.StringUtils;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RHttpTunnelClient {

	public static void main(String[] args) {
		String[] disabledCommands = { "--spring.shell.command.script.enabled=false",
				"--spring.shell.command.stacktrace.enabled=false" };
		String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);
		SpringApplication.run(RHttpTunnelClient.class, fullArgs);
	}

}
