package com.louyj.rhttptunnel.server.filter;

import static com.louyj.rhttptunnel.model.util.AESEncryptUtils.defaultKey;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.louyj.rhttptunnel.model.util.AESEncryptUtils;

/**
 * Created on 2018年3月30日
 *
 * @author Louyj
 */
public class LoggingFilter implements Filter {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public final static String LOG_REQUEST = "Request-->";
	public final static String LOG_RESPONSE = "Response-->";

	public final static String REQUEST_HEADERS = "request_headers";
	public final static String RESPONSE_HEADERS = "response_headers";
	public final static String REQUEST_BODY = "request_body";
	public final static String RESPONSE_BODY = "response_body";
	public final static String REQUEST_INFO = "request_info";
	public final static String URI = "uri";
	public final static String QUERY_PARAMS = "query_params";
	public final static String METHOD = "method";

	public LoggingFilter() {
		super();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			String host = getRemoteHost(httpServletRequest);
			String uri = httpServletRequest.getRequestURI();
			String query = httpServletRequest.getQueryString();
			String method = httpServletRequest.getMethod();
			BufferedRequestWrapper bufferedRequest = HttpContentCachedFilter.getBufferedRequest(request);
			String bufferedRequestBody = bufferedRequest.getBufferedRequestBody();
			bufferedRequestBody = AESEncryptUtils.decrypt(bufferedRequestBody, defaultKey);
			bufferedRequest.setBuffer(bufferedRequestBody);
			logRequest(bufferedRequest);
			long ss = System.currentTimeMillis();
			chain.doFilter(request, response);
			long ee = System.currentTimeMillis();

			BufferedResponseWrapper bufferedResponse = HttpContentCachedFilter.getBufferedResponse(response);
			logResponse(bufferedResponse);
			String bufferedResponseBody = bufferedResponse.getBufferedResponseBody();
			bufferedResponseBody = AESEncryptUtils.encrypt(bufferedResponseBody, defaultKey);
			resetResponseTo(bufferedResponse, bufferedResponseBody);

			if (isNotBlank(uri) && StringUtils.equals(uri, "/") == false
					&& StringUtils.endsWith(uri, "/service-registry/instance-status") == false)
				logger.info("用户{},访问{},方式:{},参数:{},用时{}毫秒,返回状态{}.", host, uri, method, query, ee - ss,
						bufferedResponse.getStatus());
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
		logger.info(LOG_REQUEST + requestBody);
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
		logger.info(LOG_RESPONSE + responseBody);
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

}
