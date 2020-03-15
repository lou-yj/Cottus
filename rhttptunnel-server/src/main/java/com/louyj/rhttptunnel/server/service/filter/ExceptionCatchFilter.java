package com.louyj.rhttptunnel.server.service.filter;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.louyj.rhttptunnel.model.message.status.RejectReason.INTERNEL_SERVER_ERROR;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.louyj.rhttptunnel.model.message.RejectMessage;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public class ExceptionCatchFilter implements Filter {

	static final Logger logger = LoggerFactory.getLogger(ExceptionCatchFilter.class);

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
			RejectMessage ack = RejectMessage.sreason(null, INTERNEL_SERVER_ERROR);
			makeResponse(cachedResonse, ack);
		}

	}

	private void makeResponse(BufferedResponseWrapper cachedResonse, Object baseResponse)
			throws JsonProcessingException {
		resetResponseTo(cachedResonse, baseResponse);
		cachedResonse.setHeader(CONTENT_TYPE, JSON_UTF_8.toString());
	}

	public void resetResponseTo(BufferedResponseWrapper cachedResonse, Object object) throws JsonProcessingException {
		resetResponseTo(cachedResonse, new Gson().toJson(object));
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
