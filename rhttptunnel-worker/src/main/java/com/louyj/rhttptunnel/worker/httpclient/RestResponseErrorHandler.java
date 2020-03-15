package com.louyj.rhttptunnel.worker.httpclient;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import com.google.common.base.Charsets;

/**
 * RestTemplate异常处理
 * <p>
 * Created on 2018年4月11日
 *
 * @author Louyj
 */
public class RestResponseErrorHandler extends DefaultResponseErrorHandler {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        try {
            super.handleError(response);
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException ee = (HttpClientErrorException) e;
                logger.error("RestTemplate Client Error: status code: {}, status text: {}, response body: {}",
                        ee.getStatusCode(), ee.getStatusText(),
                        getResponseBodyAsString(ee.getResponseBodyAsByteArray()));
            } else if (e instanceof HttpServerErrorException) {
                HttpServerErrorException ee = (HttpServerErrorException) e;
                logger.error("RestTemplate Server Error: status code: {}, status text: {}, response body: {}",
                        ee.getStatusCode(), ee.getStatusText(),
                        getResponseBodyAsString(ee.getResponseBodyAsByteArray()));
            } else if (e instanceof RestClientException) {
                RestClientException ee = (RestClientException) e;
                logger.error("RestTemplate Unknow Error: {}", ee.getMessage());
            }
            throw e;
        }
    }

    private String getResponseBodyAsString(byte[] bs) {
        return new String(bs, Charsets.UTF_8);
    }

}
