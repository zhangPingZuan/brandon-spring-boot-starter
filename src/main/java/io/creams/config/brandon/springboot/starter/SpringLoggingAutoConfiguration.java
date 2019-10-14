package io.creams.config.brandon.springboot.starter;

import io.creams.config.brandon.springboot.starter.message.CustomizeChannelInterceptorAdapter;
import io.creams.config.brandon.springboot.starter.util.UniqueIDGenerator;
import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDisruptor;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.JsonLayout;
import org.apache.logging.log4j.core.net.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * @author Snow
 * @version 1.0
 * @date 2019-09-16 18:06
 * @description
 */
@Configuration
@ConfigurationProperties(prefix = "creams.tool.brand")
@Slf4j
public class SpringLoggingAutoConfiguration {

    private String logstashHost = "localhost";
    private Integer logstashPort = 5000;
    private Boolean enabled;

    @Value("${spring.application.name:-}")
    String name;

    @Autowired(required = false)
    RestTemplate template;

    @Bean
    public UniqueIDGenerator generator() {
        return new UniqueIDGenerator();
    }

    @Bean
    public SpringLoggingFilter loggingFilter() {
        return new SpringLoggingFilter(generator(), this.name);
    }

    @Bean
    @GlobalChannelInterceptor
    public CustomizeChannelInterceptorAdapter customizeChannelInterceptorAdapter() {
        return new CustomizeChannelInterceptorAdapter(generator(), this.name);
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new RestTemplateSetHeaderInterceptor());
        restTemplate.setInterceptors(interceptorList);
        return restTemplate;
    }


    @EventListener(ApplicationStartedEvent.class)
    public void doSomethingAfterStartup() {
        if (this.enabled != null && this.enabled) {
            final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            final org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();

            // 启动异步发送
            AsyncLoggerConfigDisruptor asyncLoggerConfigDisruptor = (AsyncLoggerConfigDisruptor) config
                    .getAsyncLoggerConfigDelegate();
            asyncLoggerConfigDisruptor.start();

            // 使用socket appender 发送到logstash
            Appender socketAppender = SocketAppender.newBuilder()
                    .setName("logstash")
                    .withHost(logstashHost)
                    .withPort(logstashPort)
                    .setLayout(JsonLayout.createDefaultLayout())
                    .withProtocol(Protocol.TCP)
                    .setConfiguration(config)
                    .build();
            socketAppender.start();
            config.addAppender(socketAppender);

            // 创建appender 引用
            AppenderRef ref = AppenderRef.createAppenderRef(socketAppender.getName(), null, null);
            AppenderRef[] refs = new AppenderRef[]{ref};
            LoggerConfig springLoggingFilterLoggerConfig = AsyncLoggerConfig
                    .createLogger(true, Level.ALL, "io.creams.config.brandon.springboot.starter.SpringLoggingFilter",
                            "true", refs, null, config, null);
            LoggerConfig customizeChannelInterceptorAdapterLoggerConfig = AsyncLoggerConfig
                    .createLogger(true, Level.ALL, "io.creams.config.brandon.springboot.starter.message.CustomizeChannelInterceptorAdapter",
                            "true", refs, null, config, null);

            // 添加配置
            springLoggingFilterLoggerConfig.addAppender(socketAppender, Level.ALL, null);
            customizeChannelInterceptorAdapterLoggerConfig.addAppender(socketAppender, Level.ALL, null);

            // 添加logger
            config.addLogger(springLoggingFilterLoggerConfig.getName(), springLoggingFilterLoggerConfig);
            config.addLogger(customizeChannelInterceptorAdapterLoggerConfig.getName(), customizeChannelInterceptorAdapterLoggerConfig);
            ctx.updateLoggers();
            log.info("###### BRANDON start up and logstash context init successfully");
        }
    }

    @PostConstruct
    public void init() {
        log.debug("SpringLoggingAutoConfiguration init method: {}", template);
        Optional.ofNullable(template).ifPresent(restTemplate -> {
            List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<>();
            interceptorList.add(new RestTemplateSetHeaderInterceptor());
            restTemplate.getInterceptors().addAll(interceptorList);
        });
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getLogstashHost() {
        return logstashHost;
    }

    public void setLogstashHost(String logstashHost) {
        this.logstashHost = logstashHost;
    }

    public Integer getLogstashPort() {
        return logstashPort;
    }

    public void setLogstashPort(Integer logstashPort) {
        this.logstashPort = logstashPort;
    }
}
