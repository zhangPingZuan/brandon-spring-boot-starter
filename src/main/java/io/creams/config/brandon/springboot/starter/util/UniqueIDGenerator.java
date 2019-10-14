package io.creams.config.brandon.springboot.starter.util;

import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Snow
 * @version 1.0
 * @date 2019-09-16 18:04
 * @description
 */
public class UniqueIDGenerator {

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";

    public void generateAndSetMDC(HttpServletRequest request) {

        MDC.clear();

        // 保证当前服务唯一
        MDC.put(REQUEST_ID_HEADER_NAME, UUID.randomUUID().toString());

        String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
        if (correlationId == null)
            correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_HEADER_NAME, correlationId);
    }

    public void generateAndSetMDC(Message<?> message){

        MDC.clear();

        // 保证当前服务唯一
        MDC.put(REQUEST_ID_HEADER_NAME, UUID.randomUUID().toString());

        String correlationId =Optional.ofNullable(message.getHeaders().get(CORRELATION_ID_HEADER_NAME)).map(Object::toString).orElse("");
        if (StringUtils.isEmpty(correlationId))
            correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_HEADER_NAME, correlationId);


    }

}
