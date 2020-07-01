package com.louyj.rhttptunnel.server.filter;

import static com.louyj.rhttptunnel.model.util.AESEncryptUtils.defaultKey;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.util.AESEncryptUtils;

/**
 * Created on 2018年3月30日
 *
 * @author Louyj
 */
public class EncryptFilter implements Filter {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public final static String LOG_EXCHANGE = "Exchange-->";

	public EncryptFilter() {
		super();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			BufferedRequestWrapper bufferedRequest = HttpContentCachedFilter.getBufferedRequest(request);
			String bufferedRequestBody = bufferedRequest.getBufferedRequestBody();
			bufferedRequestBody = AESEncryptUtils.decrypt(bufferedRequestBody, defaultKey);
			bufferedRequest.setBuffer(bufferedRequestBody);
			chain.doFilter(request, response);
			BufferedResponseWrapper bufferedResponse = HttpContentCachedFilter.getBufferedResponse(response);
			String bufferedResponseBody = bufferedResponse.getBufferedResponseBody();
			bufferedResponseBody = AESEncryptUtils.encrypt(bufferedResponseBody, defaultKey);
			resetResponseTo(bufferedResponse, bufferedResponseBody);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void resetResponseTo(BufferedResponseWrapper cachedResonse, String response) {
		try {
			cachedResonse.resetBuffer();
			cachedResonse.getWriter().write(response);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public void destroy() {

	}

}
