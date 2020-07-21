package com.louyj.rhttptunnel.model.http;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.CLIENT_ERROR;
import static com.louyj.rhttptunnel.model.util.AESEncryptUtils.defaultKey;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import com.louyj.rhttptunnel.model.config.IConfigListener;
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
public class MessageExchanger implements InitializingBean, DisposableBean, IConfigListener {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String HTTP_SOCKET_TIMEOUT = "http.socketTimeout";
	private static final String HTTP_CONNECT_TIMEOUT = "http.connectTimeout";
	private static final String HTTP_MAX_POOL_SIZE = "http.maxPoolSize";
	private static final String HTTP_MAX_PER_ROUTE = "http.maxPerRoute";
	private static final String HTTP_VERBOSE = "http.verbose";

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

	private boolean verbose = false;

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
		initHttpClient();
	}

	private void initHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		}).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, ALLOW_ALL_HOSTNAME_VERIFIER);
		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
		poolingConnManager.setMaxTotal(maxPoolSize);
		poolingConnManager.setDefaultMaxPerRoute(maxPerRoute);
		CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(poolingConnManager)
				.setSSLSocketFactory(sslsf).build();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout)
				.setConnectTimeout(connectTimeout).build();
		CloseableHttpClient httpclientOld = this.httpclient;
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
		if (httpclientOld != null) {
			IOUtils.closeQuietly(httpclientOld);
		}
	}

	public final BaseMessage jsonPost(String endpoint, BaseMessage message) {
		for (int i = 0; i < serverAddresses.size(); i++) {
			BaseMessage baseMessage = jsonPostOnce(endpoint, message);
			if (baseMessage != null) {
				return baseMessage;
			}
			currentServerIndex++;
		}
		if (verbose) {
			logger.info(String.format("Server %s commucation failed, try another",
					serverAddresses.get(Math.abs(currentServerIndex % serverAddresses.size()))));
		}
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
			if (verbose) {
				logger.info("Send message {}", data);
			}
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
				if (verbose) {
					logger.info("Receive response {}", json);
				}
				return jackson.readValue(json, BaseMessage.class);
			} finally {
				response.close();
			}
		} catch (ConnectTimeoutException | SocketException e) {
			if (verbose) {
				logger.error("", e);
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e2) {
			}
			return null;
		} catch (Exception e) {
			if (verbose) {
				logger.error("", e);
			}
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

	@Override
	public List<String> keys() {
		return Lists.newArrayList(HTTP_CONNECT_TIMEOUT, HTTP_MAX_PER_ROUTE, HTTP_MAX_POOL_SIZE, HTTP_SOCKET_TIMEOUT,
				HTTP_VERBOSE);
	}

	@Override
	public String value(String clientId, String key) {
		switch (key) {
		case HTTP_CONNECT_TIMEOUT:
			return String.valueOf(this.connectTimeout);
		case HTTP_MAX_PER_ROUTE:
			return String.valueOf(this.maxPerRoute);
		case HTTP_MAX_POOL_SIZE:
			return String.valueOf(this.maxPoolSize);
		case HTTP_SOCKET_TIMEOUT:
			return String.valueOf(this.socketTimeout);
		case HTTP_VERBOSE:
			return String.valueOf(this.verbose);
		default:
			break;
		}
		return null;
	}

	@Override
	public void onChanged(String clientId, String key, String value) {
		try {
			switch (key) {
			case HTTP_CONNECT_TIMEOUT:
				this.connectTimeout = NumberUtils.toInt(value, this.connectTimeout);
				initHttpClient();
				break;
			case HTTP_MAX_PER_ROUTE:
				this.maxPerRoute = NumberUtils.toInt(value, this.maxPerRoute);
				initHttpClient();
				break;
			case HTTP_MAX_POOL_SIZE:
				this.maxPoolSize = NumberUtils.toInt(value, this.maxPoolSize);
				initHttpClient();
				break;
			case HTTP_SOCKET_TIMEOUT:
				this.socketTimeout = NumberUtils.toInt(value, this.socketTimeout);
				initHttpClient();
				break;
			case HTTP_VERBOSE:
				this.verbose = BooleanUtils.toBoolean(value);
			default:
				break;
			}
		} catch (Exception e) {
			if (verbose) {
				logger.error("", e);
			}
		}
	}

}
