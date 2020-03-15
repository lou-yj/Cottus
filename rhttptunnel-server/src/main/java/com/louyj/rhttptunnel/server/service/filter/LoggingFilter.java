package com.louyj.rhttptunnel.server.service.filter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.louyj.rhttptunnel.server.util.JsonUtils;

/**
 * Created on 2018年3月30日
 *
 * @author Louyj
 */
public class LoggingFilter implements Filter {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public final static String LOG_REQUEST = "Request params-->";
	public final static String LOG_RESPONSE = "Response-->";

	public final static String REQUEST_HEADERS = "request_headers";
	public final static String RESPONSE_HEADERS = "response_headers";
	public final static String REQUEST_BODY = "request_body";
	public final static String RESPONSE_BODY = "response_body";
	public final static String REQUEST_INFO = "request_info";
	public final static String URI = "uri";
	public final static String QUERY_PARAMS = "query_params";
	public final static String METHOD = "method";

	private ObjectMapper jackson = JsonUtils.jackson();

	public LoggingFilter() {
		super();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String host = getRemoteHost(httpServletRequest);
		String uri = httpServletRequest.getRequestURI();
		String query = httpServletRequest.getQueryString();
		String method = httpServletRequest.getMethod();
		long ss = System.currentTimeMillis();
		chain.doFilter(request, response);
		long ee = System.currentTimeMillis();

		if (logger.isDebugEnabled()) {
			try {
				BufferedRequestWrapper bufferedRequest = HttpContentCachedFilter.getBufferedRequest(request);
				logRequest(bufferedRequest);
				BufferedResponseWrapper bufferedResponse = HttpContentCachedFilter.getBufferedResponse(response);
				logResponse(bufferedResponse);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		if (isNotBlank(uri) && StringUtils.equals(uri, "/") == false
				&& StringUtils.endsWith(uri, "/service-registry/instance-status") == false)
			logger.info("用户{},访问{},方式:{},参数:{},用时{}毫秒,返回状态{}.", host, uri, method, query, ee - ss,
					httpServletResponse.getStatus());

	}

	private void logRequest(BufferedRequestWrapper bufferedRequest) throws JsonProcessingException {
		if (bufferedRequest == null) {
			logger.info(LOG_REQUEST + "Not Supported.");
			return;
		}
		String requestBody = null;
		if (bufferedRequest.isBinaryStream()) {
			requestBody = String.format("Binary stream, Content-Type: %s, Content-Length: %d",
					bufferedRequest.getContentType(), bufferedRequest.getContentLength());
		} else {
			requestBody = bufferedRequest.getBufferedRequestBody();
		}

		Map<String, Object> requestInfo = new HashMap<>();
		requestInfo.put(URI, bufferedRequest.getRequestURI());
		requestInfo.put(QUERY_PARAMS, bufferedRequest.getQueryString());
		requestInfo.put(METHOD, bufferedRequest.getMethod());

		Map<String, Object> requestLog = new HashMap<>();
		requestLog.put(REQUEST_BODY, tryParseJson(requestBody));
		requestLog.put(REQUEST_HEADERS, headersMap(bufferedRequest));
		requestLog.put(REQUEST_INFO, requestInfo);

		logger.info(LOG_REQUEST + jackson.writeValueAsString(requestLog));
	}

	private void logResponse(BufferedResponseWrapper bufferedResponse) throws Exception {
		if (bufferedResponse == null) {
			logger.info(LOG_RESPONSE + "Not Supported.");
			return;
		}
		String responseBody = null;
		if (bufferedResponse.isBinaryStream()) {
			responseBody = String.format("Binary stream, Content-Type: %s, Content-Length: %d",
					bufferedResponse.getContentType(), bufferedResponse.getContentSize());
		} else {
			responseBody = bufferedResponse.getBufferedResponseBody();
		}
		Map<String, Object> responseLog = new HashMap<>();
		responseLog.put(RESPONSE_BODY, tryParseJson(responseBody));
		responseLog.put(RESPONSE_HEADERS, headersMap(bufferedResponse));
		logger.info(LOG_RESPONSE + jackson.writeValueAsString(responseLog));
	}

	public static String getRemoteHost(javax.servlet.http.HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}

	@Override
	public void destroy() {

	}

	private Object tryParseJson(String json) {
		try {
			return jackson.readValue(json, Object.class);
		} catch (IOException e) {
			return json;
		}
	}

	private Map<String, Object> headersMap(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader(headerName);
			result.put(headerName, headerValue);
		}
		return result;
	}

	private Map<String, Object> headersMap(HttpServletResponse response) {
		Map<String, Object> result = new HashMap<>();
		Collection<String> headerNames = response.getHeaderNames();
		for (String headerName : headerNames) {
			String headerValue = response.getHeader(headerName);
			result.put(headerName, headerValue);
		}
		return result;
	}
}
