package com.louyj.rhttptunnel.server.service.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对Request Body做缓存, 以便对输入流多次读取及修改
 * <p>
 * 缓存有大小限制, 超过maxContentLength的报文不会被缓存.
 * <p>
 * Created on 2018年1月2日
 *
 * @author Louyj
 */
public class BufferedRequestWrapper extends HttpServletRequestWrapper {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private ServletInputStream inputStream;

    private byte[] buffer;

    public BufferedRequestWrapper(HttpServletRequest request) throws IOException {
        this(request, 1 << 10 << 10);
    }

    public BufferedRequestWrapper(HttpServletRequest request, int maxContentLength) throws IOException {
        super(request);
        int contentLength = request.getContentLength();
        this.inputStream = request.getInputStream();
        if (contentLength > maxContentLength) {
            logger.warn("Request body to large. url {} length {}", request.getRequestURI(), contentLength);
        } else {
            buffer = IOUtils.toByteArray(request.getInputStream());
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (buffer == null) {
            return this.inputStream;
        } else {
            return new BufferedServletInputStream(buffer);
        }
    }

    public void setBuffer(String content) {
        try {
            buffer = content.getBytes(getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
        }
    }

    @Override
    public int getContentLength() {
        if (this.buffer != null) {
            return this.buffer.length;
        }
        return super.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        if (this.buffer != null) {
            return this.buffer.length;
        }
        return super.getContentLengthLong();
    }

    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return (enc != null ? enc : "UTF-8" /* WebUtils.DEFAULT_CHARACTER_ENCODING */);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    public String getBufferedRequestBody() {
        if (buffer != null) {
            try {
                return new String(buffer, getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                logger.error("", e);
            }
        }
        return null;
    }

    public boolean isBinaryStream() {
        String contentType = getContentType();
        if (StringUtils.contains(contentType, "multipart")) {
            return true;
        }
        return false;
    }

    private class BufferedServletInputStream extends ServletInputStream {

        private ByteArrayInputStream bais;

        public BufferedServletInputStream(byte[] buffer) {
            this.bais = new ByteArrayInputStream(buffer);
        }

        @Override
        public int read() throws IOException {
            return this.bais.read();
        }

        @Override
        public boolean isFinished() {
            return bais.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {

        }
    }

}
