package com.louyj.rhttptunnel.server.filter;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.louyj.rhttptunnel.model.message.consts.RejectReason.INTERNEL_SERVER_ERROR;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.util.JsonUtils;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ExceptionCatchFilter implements Filter {

	static final Logger logger = LoggerFactory.getLogger(ExceptionCatchFilter.class);

	private ObjectMapper jackson = JsonUtils.jacksonWithType();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		BufferedResponseWrapper cachedResonse = HttpContentCachedFilter.getBufferedResponse(response);
		BufferedRequestWrapper bufferedRequest = HttpContentCachedFilter.getBufferedRequest(request);
		try {
			chain.doFilter(request, cachedResonse);
		} catch (Exception e) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String uri = httpRequest.getRequestURI();
			logger.error("Catched Exception: uri {} method {} query string {} request body {}", uri,
					bufferedRequest.getMethod(), bufferedRequest.getQueryString(),
					bufferedRequest.getBufferedRequestBody(), e);
			BaseMessage requestMsg = jackson.readValue(bufferedRequest.getBufferedRequestBody(), BaseMessage.class);
			RejectMessage ack = RejectMessage.sreason(requestMsg.getExchangeId(), INTERNEL_SERVER_ERROR.reason());
			makeResponse(cachedResonse, ack);
		}

	}

	private void makeResponse(BufferedResponseWrapper cachedResonse, Object baseResponse)
			throws JsonProcessingException {
		resetResponseTo(cachedResonse, baseResponse);
		cachedResonse.setHeader(CONTENT_TYPE, MediaType.TEXT_PLAIN.toString());
	}

	public void resetResponseTo(BufferedResponseWrapper cachedResonse, Object object) throws JsonProcessingException {
		resetResponseTo(cachedResonse, jackson.writeValueAsString(object));
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
