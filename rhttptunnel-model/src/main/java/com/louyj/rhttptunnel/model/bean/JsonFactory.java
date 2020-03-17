package com.louyj.rhttptunnel.model.bean;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS;

import java.text.SimpleDateFormat;

import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Created on 2020年3月17日
 *
 * @author Louyj
 *
 */
public class JsonFactory {

	@Bean
	public ObjectMapper jackson() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(ALLOW_SINGLE_QUOTES, true);
		objectMapper.configure(ALLOW_UNQUOTED_CONTROL_CHARS, true);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		objectMapper.enableDefaultTyping();
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		return objectMapper;
	}

}
