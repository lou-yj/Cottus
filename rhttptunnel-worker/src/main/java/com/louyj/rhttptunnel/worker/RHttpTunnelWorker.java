package com.louyj.rhttptunnel.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@ImportResource("classpath:applicationContext.xml")
@SpringBootApplication
@EnableScheduling
public class RHttpTunnelWorker {

	public static void main(String[] args) {
		SpringApplication.run(RHttpTunnelWorker.class, args);
	}

}
