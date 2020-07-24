package com.louyj.rhttptunnel.model.http;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.CLIENT_ERROR;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
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
import com.louyj.rhttptunnel.model.config.IConfigListener;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.consts.CustomHeaders;
import com.louyj.rhttptunnel.model.message.consts.EncryptType;
import com.louyj.rhttptunnel.model.util.AESEncryptUtils;
import com.louyj.rhttptunnel.model.util.JsonUtils;
import com.louyj.rhttptunnel.model.util.RsaUtils;

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

	private ObjectMapper jackson = JsonUtils.jackson();
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

	private ThreadLocal<ExchangeContext> exchangeContext = new ThreadLocal<>();
	private Key privateKey;
	private Key publicKey;
	private String aesKey;

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}

	public Key getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(Key privateKey) {
		this.privateKey = privateKey;
	}

	public Key getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(Key publicKey) {
		this.publicKey = publicKey;
	}

	@Value("${bootstrap.servers:}")
	public void setBootstrapAddress(String serverAddress) {
		this.bootstrapServers = Lists.newArrayList(serverAddress.split(","));
		this.serverAddresses = this.bootstrapServers;
	}

	public void setServerAddresses(List<String> serverAddresses) {
		this.serverAddresses = serverAddresses;
	}

	public boolean isServerConnected() {
		return CollectionUtils.size(serverAddresses) > 0;
	}

	public ThreadLocal<ExchangeContext> getExchangeContext() {
		return exchangeContext;
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
		System.out.println("[" + CLIENT_ERROR.reason() + "] All servers not available");
		System.exit(0);
		return RejectMessage.creason(message.getClientId(), message.getExchangeId(),
				"[" + CLIENT_ERROR.reason() + "] All servers not available");
	}

	private final BaseMessage jsonPostOnce(String endpoint, BaseMessage message) {
		try {
			if (CollectionUtils.isEmpty(serverAddresses)) {
				throw new RuntimeException("No server available");
			}
			String serverAddress = serverAddresses.get(Math.abs(currentServerIndex % serverAddresses.size()));
			String reqJson = jackson.writeValueAsString(message);
			if (verbose) {
				logger.info("Send message {}", reqJson);
			}
			byte[] reqData = reqJson.getBytes(StandardCharsets.UTF_8);
			HttpPost httpPost = new HttpPost(serverAddress + endpoint);
			httpPost.setConfig(requestConfig);
			httpPost.setHeader(CONTENT_TYPE, "application/octet-stream");
			httpPost.setHeader(CustomHeaders.MESSAGE_TYPE, message.getClass().getName());
			ExchangeContext exchangeContext = this.exchangeContext.get();
			if (exchangeContext != null) {
				httpPost.setHeader(CustomHeaders.CLIENT_ID, exchangeContext.getClientId());
				Map<String, String> extraHeaderMap = exchangeContext.httpHeaders();
				if (extraHeaderMap != null) {
					for (Entry<String, String> entry : extraHeaderMap.entrySet()) {
						httpPost.setHeader(entry.getKey(), entry.getValue());
					}
				}
				EncryptType encryptType = EncryptType.NONE;
				if (aesKey != null) {
					encryptType = EncryptType.AES;
				} else if (publicKey != null) {
					encryptType = EncryptType.RSA;
				}
				switch (encryptType) {
				case RSA:
					reqData = RsaUtils.encrypt(reqData, publicKey);
					httpPost.setHeader(CustomHeaders.ENCRYPT_TYPE, EncryptType.RSA.name());
					break;
				case AES:
					reqData = AESEncryptUtils.encrypt(reqData, aesKey);
					httpPost.setHeader(CustomHeaders.ENCRYPT_TYPE, EncryptType.AES.name());
					break;
				default:
					httpPost.setHeader(CustomHeaders.ENCRYPT_TYPE, EncryptType.NONE.name());
					break;
				}
			}
			httpPost.setEntity(new ByteArrayEntity(reqData));
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				HttpEntity entity = response.getEntity();
				byte[] respData = EntityUtils.toByteArray(entity);
				Header encryptHeader = response.getFirstHeader(CustomHeaders.ENCRYPT_TYPE);
				if (encryptHeader == null) {
					throw new IllegalArgumentException("Bad Response, Missing Encrypt Type");
				}
				switch (EncryptType.of(encryptHeader.getValue())) {
				case RSA:
					respData = RsaUtils.decrypt(respData, privateKey);
					break;
				case AES:
					respData = AESEncryptUtils.decrypt(respData, aesKey);
					break;
				case NONE:
					break;
				default:
					break;
				}
				String respJson = new String(respData, UTF_8);
				Header msgType = response.getFirstHeader(CustomHeaders.MESSAGE_TYPE);
				if (msgType == null) {
					throw new IllegalArgumentException("Bad Response, Missing Message Type");
				}
				if (verbose) {
					logger.info("Receive response {}", respJson);
				}
				return (BaseMessage) jackson.readValue(respJson, Class.forName(msgType.getValue()));
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
