package com.louyj.rhttptunnel.model.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpContext;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
public class HttpRequestRetryHandler {

	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		if (executionCount >= 5) {
			// Do not retry if over max retry count
			return false;
		}
		if (exception instanceof InterruptedIOException) {
			// Timeout
			return false;
		}
		if (exception instanceof UnknownHostException) {
			// Unknown host
			return false;
		}
		if (exception instanceof ConnectTimeoutException) {
			// Connection refused
			return false;
		}
		if (exception instanceof SSLException) {
			// SSL handshake exception
			return false;
		}
		HttpClientContext clientContext = HttpClientContext.adapt(context);
		HttpRequest request = clientContext.getRequest();
		boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
		if (idempotent) {
			// Retry if the request is considered idempotent
			return true;
		}
		return false;
	}

}
