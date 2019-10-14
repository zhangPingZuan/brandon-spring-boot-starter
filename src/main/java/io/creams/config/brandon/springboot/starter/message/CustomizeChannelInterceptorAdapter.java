package io.creams.config.brandon.springboot.starter.message;

import io.creams.config.brandon.springboot.starter.Constant;
import io.creams.config.brandon.springboot.starter.util.UniqueIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.StringUtils;


/**
 * @author Snow
 * @version 1.0
 * @date 2019-09-18 16:42
 * @description
 */
@Slf4j
public class CustomizeChannelInterceptorAdapter implements ChannelInterceptor {

    private UniqueIDGenerator generator;

    private String name;

    public CustomizeChannelInterceptorAdapter(UniqueIDGenerator generator, String name) {
        this.generator = generator;
        this.name = name;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        // 全部为空，说明是一个独立的消息
        if (StringUtils.isEmpty(MDC.get(Constant.REQUEST_ID_HEADER_NAME))
                && StringUtils.isEmpty(MDC.get(Constant.CORRELATION_ID_HEADER_NAME)))
            generator.generateAndSetMDC(message);

        log.info("middleware_message: X-Request-ID={}, X-Correlation-ID={}, headers={}, payload={}, applicaiton={}",
                MDC.get(Constant.REQUEST_ID_HEADER_NAME),
                MDC.get(Constant.CORRELATION_ID_HEADER_NAME),
                message.getHeaders(),
                new String((byte[]) message.getPayload()), name);
        return message;
    }

}
