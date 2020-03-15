package com.louyj.rhttptunnel.worker.message;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import com.louyj.rhttptunnel.model.message.BaseMessage;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class Endpoints {

	public static final String EXCHANGE = "/worker/exchange";

	public static final HttpEntity<BaseMessage> jsonEntiry(BaseMessage body) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(APPLICATION_JSON);
		return new HttpEntity<BaseMessage>(body, httpHeaders);
	}

}
