package com.louyj.rhttptunnel.worker.message;

import static com.louyj.rhttptunnel.model.message.status.RejectReason.CLIENT_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.worker.ClientDetector;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class MessageExchanger {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	public RestTemplate restTemplate;

	public final BaseMessage jsonPost(String uri, BaseMessage message) {
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(APPLICATION_JSON);
			HttpEntity<BaseMessage> httpEntity = new HttpEntity<BaseMessage>(message, httpHeaders);
			return restTemplate.postForObject(uri, httpEntity, BaseMessage.class);
		} catch (Exception e) {
			logger.error("", e);
			return RejectMessage.creason(ClientDetector.WORKER, message.getExchangeId(), CLIENT_ERROR);
		}
	}

}
