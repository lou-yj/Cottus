package com.louyj.rhttptunnel.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import com.louyj.rhttptunnel.model.bean.JsonFactory;

/**
 * 
 * java -jar "-Dserver.url=http://localhost:18081" "-Ddata.dir=d:/data/ignite1"
 * "-Dserver.port=18081" -jar .\rhttptunnel-server-1.0-SNAPSHOT.jar
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
