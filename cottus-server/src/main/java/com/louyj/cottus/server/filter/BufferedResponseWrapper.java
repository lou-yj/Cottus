package com.louyj.cottus.server.filter;

import static org.apache.commons.lang3.StringUtils.lowerCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * 基于ContentCachingResponseWrapper, 修复一下问题:
 * <p>
 * 1. content缓冲区为固定大小, 溢出flush到底层Wrapper
 * <p>
 * 2. 文件类型的响应不做缓存
 * <p>
 * Created on 2018年7月20日
 *
 * @author Louyj
 */
public class BufferedResponseWrapper extends HttpServletResponseWrapper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ServletOutputStream outputStream = new ResponseServletOutputStream();
    private FastByteArrayOutputStream content;
    private PrintWriter writer;

    private int statusCode = HttpServletResponse.SC_OK;

    private Integer contentLength;
    private int maxContentLength = 1 << 10 << 10;
    private int initBufferSize = 1024;
    private boolean bufferCrashed = false;
    private int crashedBufferedSize = 0;

    public BufferedResponseWrapper(HttpServletResponse response) {
        this(response, 1 << 10 << 10);
    }

    public BufferedResponseWrapper(HttpServletResponse response, int maxContentLength) {
        super(response);
        this.maxContentLength = maxContentLength;
        this.content = new FastByteArrayOutputStream(initBufferSize);
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        this.statusCode = sc;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setStatus(int sc, String sm) {
        super.setStatus(sc, sm);
        this.statusCode = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        copyBodyToResponse(false);
        try {
            super.sendError(sc);
        } catch (IllegalStateException ex) {
            // Possibly on Tomcat when called too late: fall back to silent setStatus
            super.setStatus(sc);
        }
        this.statusCode = sc;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendError(int sc, String msg) throws IOException {
        copyBodyToResponse(false);
        try {
            super.sendError(sc, msg);
        } catch (IllegalStateException ex) {
            // Possibly on Tomcat when called too late: fall back to silent setStatus
            super.setStatus(sc, msg);
        }
        this.statusCode = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        copyBodyToResponse(false);
        super.sendRedirect(location);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.writer == null) {
            String characterEncoding = getCharacterEncoding();
            this.writer = (characterEncoding != null ? new ResponsePrintWriter(characterEncoding)
                    : new ResponsePrintWriter("UTF-8")); // WebUtils.DEFAULT_CHARACTER_ENCODING
        }
        return this.writer;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void setContentLength(int len) {
        if (len > this.content.size()) {
            this.content.resize(Math.min(maxContentLength, len));
        }
        this.contentLength = len;
    }

    // Overrides Servlet 3.1 setContentLengthLong(long) at runtime
    public void setContentLengthLong(long len) {
        if (len > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Content-Length exceeds BufferedResponseWrapper's maximum (" + Integer.MAX_VALUE + "): " + len);
        }
        int lenInt = (int) len;
        if (lenInt > this.content.size()) {
            this.content.resize(Math.min(maxContentLength, lenInt));
        }
        this.contentLength = lenInt;
    }

    @Override
    public void setBufferSize(int size) {
        if (size > this.content.size()) {
            this.content.resize(Math.min(maxContentLength, size));
        }
    }

    @Override
    public void resetBuffer() {
        this.content.reset();
    }

    @Override
    public void reset() {
        super.reset();
        this.content.reset();
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public byte[] getContentAsByteArray() {
        if (bufferCrashed) {
            logger.info("Crashed buffered, keep size {} crashed buffered size {}", this.content.size(),
                    this.crashedBufferedSize);
        }
        return this.content.toByteArray();
    }

    public String getBufferedResponseBody() throws UnsupportedEncodingException {
        String characterEncoding = getCharacterEncoding();
        if (StringUtils.isBlank(characterEncoding)) {
            characterEncoding = "UTF-8";
        }
        return new String(getContentAsByteArray(), characterEncoding);
    }

    public boolean isBinaryStream() {
        String contentType = lowerCase(getContentType());
        if (StringUtils.isBlank(contentType)) {
            return true;
        }
        return !(contentType.contains("json") || contentType.contains("text") || contentType.contains("xml"));
    }

    public InputStream getContentInputStream() {
        if (bufferCrashed) {
            logger.info("Crashed buffered, keep size {} crashed buffered size {}", this.content.size(),
                    this.crashedBufferedSize);
        }
        return this.content.getInputStream();
    }

    public int getContentSize() {
        return this.content.size() + crashedBufferedSize;
    }

    public void copyBodyToResponse() throws IOException {
        copyBodyToResponse(true);
    }

    protected void copyBodyToResponse(boolean complete) throws IOException {
        if (this.content.size() > 0) {
            HttpServletResponse rawResponse = (HttpServletResponse) getResponse();
            if ((complete || this.contentLength != null) && !rawResponse.isCommitted()) {
                rawResponse.setContentLength(
                        complete ? this.content.size() + this.crashedBufferedSize : this.contentLength);
                this.contentLength = null;
            }
            this.content.writeTo(rawResponse.getOutputStream());
            this.content.reset();
            if (complete) {
                super.flushBuffer();
            }
        }
    }

    private class ResponseServletOutputStream extends ServletOutputStream {

        @Override
        public void write(int b) throws IOException {
            keepBufferInSize(1);
            content.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            keepBufferInSize(len);
            content.write(b, off, len);
        }

        private void keepBufferInSize(int incrSize) throws IOException {
            if (content.size() + incrSize >= maxContentLength) {
                int contentSize = content.size();
                crashedBufferedSize += contentSize;
                copyBodyToResponse(false);
                logger.warn(
                        "Buffer overflow, copy to under stream. Max buffer {} current {} append {} truncated {}. Ignore when file upload/download operation.",
                        maxContentLength, contentSize, incrSize, crashedBufferedSize);
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
        }
    }

    private class ResponsePrintWriter extends PrintWriter {

        public ResponsePrintWriter(String characterEncoding) throws UnsupportedEncodingException {
            super(new OutputStreamWriter(outputStream, characterEncoding));
        }

        @Override
        public void write(char buf[], int off, int len) {
            super.write(buf, off, len);
            super.flush();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
        }
    }

}
