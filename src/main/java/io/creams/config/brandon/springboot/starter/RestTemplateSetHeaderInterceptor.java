package io.creams.config.brandon.springboot.starter;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author Snow
 * @version 1.0
 * @date 2019-09-17 11:05
 * @description
 */
public class RestTemplateSetHeaderInterceptor implements ClientHttpRequestInterceptor {

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add(Constant.REQUEST_ID_HEADER_NAME, MDC.get(Constant.REQUEST_ID_HEADER_NAME));
        request.getHeaders().add(Constant.CORRELATION_ID_HEADER_NAME, MDC.get(Constant.CORRELATION_ID_HEADER_NAME));
        return execution.execute(request, body);
    }

}