package com.louyj.rhttptunnel.worker;

import java.net.InetAddress;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${sys.hostname:}")
	private String hostname;

	@Value("${sys.ip:}")
	private String ip;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (StringUtils.isNotBlank(this.ip)) {
			CLIENT.setIp(this.ip);
		} else {
			InetAddress localHost = InetAddress.getLocalHost();
			String ip = localHost.getHostAddress();
			CLIENT.setIp(ip);
		}
		if (StringUtils.isNotBlank(this.hostname)) {
			CLIENT.setHost(this.hostname);
		} else {
			InetAddress localHost = InetAddress.getLocalHost();
			String hostName = localHost.getHostName();
			CLIENT.setHost(hostName);
		}
		logger.info("host name {} ip {} id {}", CLIENT.getHost(), CLIENT.getIp(), CLIENT.getUuid());
	}

}
