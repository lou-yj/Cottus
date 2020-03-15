package com.louyj.rhttptunnel.server.util;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("deprecation")
public class JsonUtils {

	static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

	public static Gson gson() {
		return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	}

	public static ObjectMapper jackson() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(ALLOW_SINGLE_QUOTES, true);
		objectMapper.configure(ALLOW_UNQUOTED_CONTROL_CHARS, true);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		return objectMapper;
	}

	public static ObjectMapper jacksonWithType() {
		ObjectMapper objectMapper = jackson();
		objectMapper.enableDefaultTyping();
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		return objectMapper;
	}

	public static ObjectMapper jacksonWithFieldAccessable() {
		ObjectMapper objectMapper = jacksonWithType();
		objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(Visibility.ANY).withGetterVisibility(Visibility.NONE)
				.withSetterVisibility(Visibility.NONE).withCreatorVisibility(Visibility.ANY));
		return objectMapper;
	}

}
