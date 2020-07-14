package com.louyj.rhttptunnel.model.http;

import static com.google.common.base.Charsets.UTF_8;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.CLIENT_ERROR;
import static com.louyj.rhttptunnel.model.util.AESEncryptUtils.defaultKey;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
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

	private ObjectMapper jackson = JsonUtils.jacksonWithType();

	@Value("${server.location:unknow}")
	private String serverAddress;

	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;

	@Override
	public void afterPropertiesSet() throws Exception {
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		}).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, ALLOW_ALL_HOSTNAME_VERIFIER);
		httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		requestConfig = RequestConfig.custom().setSocketTimeout(600000).setConnectTimeout(5000).build();
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public final BaseMessage jsonPost(String endpoint, BaseMessage message) {
		try {
			String data = jackson.writeValueAsString(message);
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
				return jackson.readValue(json, BaseMessage.class);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e2) {
			}
			return RejectMessage.creason(message.getClient(), message.getExchangeId(),
					"[" + CLIENT_ERROR.reason() + "]" + e.getClass().getSimpleName() + ":" + e.getMessage());
		}
	}

	@Override
	public void destroy() throws Exception {
		IOUtils.closeQuietly(httpclient);
	}

}
