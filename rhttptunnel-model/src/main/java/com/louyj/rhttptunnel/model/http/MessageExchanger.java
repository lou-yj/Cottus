package com.louyj.rhttptunnel.model.http;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.rhttptunnel.model.message.status.RejectReason.CLIENT_ERROR;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@Component
public class MessageExchanger implements InitializingBean, DisposableBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ObjectMapper jackson;

	@Value("${server.address}")
	private String serverAddress;

	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;

	@Override
	public void afterPropertiesSet() throws Exception {
		httpclient = HttpClients.createDefault();
		requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();
	}

	public final BaseMessage jsonPost(String endpoint, BaseMessage message) {
		try {
			HttpEntity httpEntity = new StringEntity(jackson.writeValueAsString(message), UTF_8);
			HttpPost httpPost = new HttpPost(serverAddress + endpoint);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(httpEntity);
			httpPost.setHeader(CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String json = EntityUtils.toString(entity, UTF_8);
				return jackson.readValue(json, BaseMessage.class);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			logger.error("", e);
			return RejectMessage.creason(message.getClient(), message.getExchangeId(), CLIENT_ERROR);
		}
	}

	@Override
	public void destroy() throws Exception {
		IOUtils.closeQuietly(httpclient);
	}

}
