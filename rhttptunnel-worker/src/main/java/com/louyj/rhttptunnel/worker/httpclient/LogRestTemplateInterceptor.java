package com.louyj.rhttptunnel.worker.httpclient;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.base.Charsets;

/**
 * RestTemplate日志拦截
 * <p>
 * Created on 2018年4月13日
 *
 * @author Louyj
 */
public class LogRestTemplateInterceptor implements ClientHttpRequestInterceptor {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		if (logger.isDebugEnabled())
			logger.info("REST Request: uri {} method {} headers {} body {}", request.getURI(), request.getMethod(),
					request.getHeaders(), new String(body, Charsets.UTF_8));

		ClientHttpResponse response = execution.execute(request, body);
		BufferingClientHttpResponseWrapper responseWrapper = null;
		if (response instanceof BufferingClientHttpResponseWrapper) {
			responseWrapper = (BufferingClientHttpResponseWrapper) response;
		} else {
			responseWrapper = new BufferingClientHttpResponseWrapper(response);
		}
		if (logger.isDebugEnabled())
			logger.info("REST Response: uri {} response {}", request.getURI(),
					IOUtils.toString(responseWrapper.getBody()));

		return responseWrapper;
	}

}
