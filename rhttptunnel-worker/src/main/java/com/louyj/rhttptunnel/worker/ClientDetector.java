package com.louyj.rhttptunnel.worker;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.ClientInfo;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class ClientDetector implements ApplicationRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final ClientInfo WORKER = new ClientInfo("UNINITED", "UNINITED");

	@Override
	public void run(ApplicationArguments args) throws Exception {
		InetAddress localHost = InetAddress.getLocalHost();
		String ip = localHost.getHostAddress();
		String hostName = localHost.getHostName();
		WORKER.setIp(ip);
		WORKER.setHost(hostName);
		logger.info("host name {} ip {} ", hostName, ip);
	}

}
