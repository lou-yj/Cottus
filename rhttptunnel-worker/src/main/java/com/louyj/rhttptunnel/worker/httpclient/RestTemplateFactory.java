package com.louyj.rhttptunnel.worker.httpclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate构造
 * <p>
 * 日志拦截打印
 * <p>
 * Created on 2018年4月13日
 *
 * @author Louyj
 */
@Component
@Import({ LogRestTemplateInterceptor.class })
public class RestTemplateFactory implements ApplicationContextAware {

	final Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new RestResponseErrorHandler());

		Map<String, ClientHttpRequestInterceptor> beans = applicationContext
				.getBeansOfType(ClientHttpRequestInterceptor.class);
		if (beans == null || beans.isEmpty()) {
			return restTemplate;
		}
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		if (interceptors == null) {
			interceptors = new ArrayList<>();
		}
		for (String key : beans.keySet()) {
			ClientHttpRequestInterceptor bean = beans.get(key);
			if (!exists(interceptors, bean)) {
				logger.info("Add RestTemplate interceptor {}", key, bean.getClass().getName());
				interceptors.add(bean);
			}
		}
		restTemplate.setInterceptors(interceptors);

		// disable accept-charset header
		for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
			if (converter instanceof StringHttpMessageConverter) {
				((StringHttpMessageConverter) converter).setWriteAcceptCharset(false);
			}
		}

		return restTemplate;
	}

	private boolean exists(List<ClientHttpRequestInterceptor> interceptors, ClientHttpRequestInterceptor bean) {
		for (ClientHttpRequestInterceptor inte : interceptors) {
			if (inte == null) {
				continue;
			}
			if (StringUtils.equals(inte.getClass().getName(), bean.getClass().getName())) {
				return true;
			}
		}
		return false;
	}

}
