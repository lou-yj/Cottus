package com.louyj.rhttptunnel.model.http;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.CLIENT_ERROR;
import static com.louyj.rhttptunnel.model.util.AESEncryptUtils.defaultKey;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import java.net.SocketException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.util.AESEncryptUtils;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
@SuppressWarnings("deprecation")
@Component
public class MessageExchanger implements InitializingBean, DisposableBean {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ObjectMapper jackson = JsonUtils.jacksonWithType();

	private List<String> bootstrapServers = Lists.newArrayList();
	private List<String> serverAddresses = Lists.newArrayList();
	private int currentServerIndex = 0;

	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;

	@Value("${http.socketTimeout:600000}")
	private int socketTimeout;
	@Value("${http.connectTimeout:5000}")
	private int connectTimeout;
	@Value("${http.maxPoolSize:200}")
	private int maxPoolSize;
	@Value("${http.maxPerRoute:50}")
	private int maxPerRoute;

	@Value("${bootstrap.servers:}")
	public void setBootstrapAddress(String serverAddress) {
		this.bootstrapServers = Lists.newArrayList(serverAddress.split(","));
		this.serverAddresses = this.bootstrapServers;
	}

	public void setServerAddresses(List<String> serverAddresses) {
		this.serverAddresses = serverAddresses;
	}

	public boolean isServerConnected() {
		return CollectionUtils.isNotEmpty(serverAddresses);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		}).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, ALLOW_ALL_HOSTNAME_VERIFIER);
		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setMaxTotal(maxPoolSize);
		poolingConnManager.setDefaultMaxPerRoute(maxPerRoute);
		httpclient = HttpClients.custom().setConnectionManager(poolingConnManager).setSSLSocketFactory(sslsf).build();
		requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
				.build();
	}

	public final BaseMessage jsonPost(String endpoint, BaseMessage message) {
		for (int i = 0; i < serverAddresses.size(); i++) {
			BaseMessage baseMessage = jsonPostOnce(endpoint, message);
			if (baseMessage != null) {
				return baseMessage;
			}
			currentServerIndex++;
		}
		logger.debug(String.format("Server %s commucation failed, try another",
				serverAddresses.get(Math.abs(currentServerIndex % serverAddresses.size()))));
		return RejectMessage.creason(message.getClientId(), message.getExchangeId(),
				"[" + CLIENT_ERROR.reason() + "] All servers not available");
	}

	private final BaseMessage jsonPostOnce(String endpoint, BaseMessage message) {
		try {
			if (CollectionUtils.isEmpty(serverAddresses)) {
				throw new RuntimeException("No server available");
			}
			String serverAddress = serverAddresses.get(Math.abs(currentServerIndex % serverAddresses.size()));
			String data = jackson.writeValueAsString(message);
			logger.debug("Send message {}", data);
			data = AESEncryptUtils.encrypt(data, defaultKey);
			HttpEntity httpEntity = new StringEntity(data, UTF_8);
			HttpPost httpPost = new HttpPost(serverAddress + endpoint);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(httpEntity);
			httpPost.setHeader(CONTENT_TYPE, MediaType.PLAIN_TEXT_UTF_8.toString());
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				HttpEntity entity = response.getEntity();
				String json = EntityUtils.toString(entity, UTF_8);
				json = AESEncryptUtils.decrypt(json, defaultKey);
				logger.debug("Receive response {}", json);
				return jackson.readValue(json, BaseMessage.class);
			} finally {
				response.close();
			}
		} catch (ConnectTimeoutException | SocketException e) {
			logger.debug("", e);
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e2) {
			}
			return null;
		} catch (Exception e) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e2) {
			}
			return RejectMessage.creason(message.getClientId(), message.getExchangeId(),
					"[" + CLIENT_ERROR.reason() + "]" + e.getClass().getSimpleName() + ":" + e.getMessage());
		}
	}

	@Override
	public void destroy() throws Exception {
		IOUtils.closeQuietly(httpclient);
	}

}
