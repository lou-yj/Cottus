package com.louyj.cottus.client;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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
public class ClientDetector implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static final ClientInfo CLIENT = new ClientInfo("UNINITED", "UNINITED");

	@Override
	public void afterPropertiesSet() throws Exception {
		InetAddress localHost = InetAddress.getLocalHost();
		String ip = localHost.getHostAddress();
		String hostName = localHost.getHostName();
		CLIENT.setIp(ip);
		CLIENT.setHost(hostName);
		logger.info("host name {} ip {} ", hostName, ip);
	}

}
