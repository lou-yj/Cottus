package com.louyj.rhttptunnel.server

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.context.annotation.{Bean, Configuration}


@Configuration
class AppConfig {

  @Bean
  def objectMapper(): ObjectMapper = {
    val objectMapper = new ObjectMapper()
    objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
    objectMapper.registerModule(DefaultScalaModule)
  }

}
