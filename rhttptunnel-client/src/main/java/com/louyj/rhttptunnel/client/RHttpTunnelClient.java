package com.louyj.rhttptunnel.client;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.shell.SpringShellAutoConfiguration;
import org.springframework.shell.jline.JLineShellAutoConfiguration;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
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
		List<String> disableCommands = Lists.newArrayList();
		if (equalsIgnoreCase(System.getProperty("stacktrace.enable"), "true") == false) {
			disableCommands.add("--spring.shell.command.stacktrace.enabled=false");
		}
		if (equalsIgnoreCase(System.getProperty("script.enable"), "true") == false) {
			disableCommands.add("--spring.shell.command.script.enabled=false");
		}
		String[] fullArgs = StringUtils.concatenateStringArrays(args, disableCommands.toArray(new String[0]));
		SpringApplication.run(RHttpTunnelClient.class, fullArgs);
	}

}
