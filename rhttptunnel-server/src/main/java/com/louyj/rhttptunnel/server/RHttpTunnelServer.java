package com.louyj.rhttptunnel.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import com.louyj.rhttptunnel.model.bean.JsonFactory;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@ImportResource("classpath:applicationContext.xml")
@SpringBootApplication
@Import({ JsonFactory.class })
public class RHttpTunnelServer {

	public static void main(String[] args) {
		SpringApplication.run(RHttpTunnelServer.class, args);
	}

}
