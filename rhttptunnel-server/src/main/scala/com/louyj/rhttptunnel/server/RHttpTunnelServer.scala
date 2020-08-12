package com.louyj.rhttptunnel.server

import com.louyj.rhttptunnel.model.bean.JsonFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Import, ImportResource}


@ImportResource(Array("classpath:applicationContext.xml"))
@SpringBootApplication
@Import(Array(classOf[JsonFactory]))
class RHttpTunnelServer

object RHttpTunnelServer {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[RHttpTunnelServer], args: _*)
  }
}
