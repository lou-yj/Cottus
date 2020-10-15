package com.louyj.cottus.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.louyj.rhttptunnel.model.http.MessageExchanger;

/**
 * 
 * java -Dbootstrap.servers=http://localhost:18081 -cp
 * dependency/*:rhttptunnel-worker-1.0-SNAPSHOT.jar
 * com.louyj.rhttptunnel.worker.RHttpTunnelWorker
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@SpringBootApplication
@Import({ MessageExchanger.class })
public class RHttpTunnelWorker {

	public static void main(String[] args) {
		SpringApplication.run(RHttpTunnelWorker.class, args);
	}

}
