package com.louyj.rhttptunnel.server.service.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * 对Request和Response做报文缓存处理, 以便在Filter链中多次操作
 * <p>
 * Created on 2018年1月2日
 *
 * @author Louyj
 */
public class HttpContentCachedFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(HttpContentCachedFilter.class);

    private int maxContentLength = 1 << 10 << 10;

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(Charsets.UTF_8.name());
        response.setCharacterEncoding(Charsets.UTF_8.name());
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        BufferedRequestWrapper cachedRequest = new BufferedRequestWrapper(httpServletRequest, maxContentLength);
        BufferedResponseWrapper cachedResonse = new BufferedResponseWrapper(httpServletResponse, maxContentLength);

        chain.doFilter(cachedRequest, cachedResonse);

        cachedResonse.copyBodyToResponse();
    }

    @Override
    public void destroy() {

    }

    public static BufferedResponseWrapper getBufferedResponse(ServletResponse response) {
        if (response instanceof BufferedResponseWrapper) {
            return (BufferedResponseWrapper) response;
        } else if (response instanceof HttpServletResponseWrapper) {
            return getBufferedResponse(((HttpServletResponseWrapper) response).getResponse());
        }
        return null;
    }

    public static BufferedRequestWrapper getBufferedRequest(ServletRequest request) {
        if (request instanceof BufferedRequestWrapper) {
            return (BufferedRequestWrapper) request;
        } else if (request instanceof HttpServletRequestWrapper) {
            return getBufferedRequest(((HttpServletRequestWrapper) request).getRequest());
        }
        return null;
    }

    public static void resetResponseBody(BufferedResponseWrapper response, String responseBody) {
        try {
            response.resetBuffer();
            response.getWriter().write(responseBody);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
