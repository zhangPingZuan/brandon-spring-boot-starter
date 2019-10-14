package io.creams.config.brandon.springboot.starter;


import io.creams.config.brandon.springboot.starter.util.UniqueIDGenerator;
import io.creams.config.brandon.springboot.starter.wrapper.SpringRequestWrapper;
import io.creams.config.brandon.springboot.starter.wrapper.SpringResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toMap;

/**
 * @author Snow
 * @version 1.0
 * @date 2019-09-16 17:51
 * @description
 */
@Slf4j
public class SpringLoggingFilter extends OncePerRequestFilter {

    private UniqueIDGenerator generator;

    private List<String> excludePaths;

    private String name;

    public SpringLoggingFilter(UniqueIDGenerator generator, String name) {
        this.generator = generator;
        this.name = name;
        excludePaths = new ArrayList<>();
        excludePaths.add("/actuator/health");
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        generator.generateAndSetMDC(request);

        // begin stopWatch
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // wrap request
        final SpringRequestWrapper wrappedRequest = new SpringRequestWrapper(request);
        if (!excludePaths.contains(wrappedRequest.getRequestURI()))
            log.info("http_request: X-Request-ID={}, X-Correlation-ID={}, method={}, uri={} ,headers={}, params={} ,payload={}, application={}",
                    MDC.get(Constant.REQUEST_ID_HEADER_NAME),
                    MDC.get(Constant.CORRELATION_ID_HEADER_NAME),
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    Collections.list(request.getHeaderNames()).stream().collect(toMap(hn -> hn, request::getHeader)),
                    Collections.list(request.getParameterNames()).stream().collect(toMap(pa -> pa, request::getParameter)),
                    IOUtils.toString(wrappedRequest.getInputStream(),
                            wrappedRequest.getCharacterEncoding()),
                    this.name);

        // wrap response
        final SpringResponseWrapper wrappedResponse = new SpringResponseWrapper(response);
        wrappedResponse.setHeader(Constant.REQUEST_ID_HEADER_NAME, MDC.get(Constant.REQUEST_ID_HEADER_NAME));
        wrappedResponse.setHeader(Constant.CORRELATION_ID_HEADER_NAME, MDC.get(Constant.CORRELATION_ID_HEADER_NAME));
        chain.doFilter(wrappedRequest, wrappedResponse);

        // stop
        stopWatch.stop();
        if (!excludePaths.contains(wrappedRequest.getRequestURI()))
            log.info("http_response({} ms): X-Request-ID={}, X-Correlation-ID={}, method={}, uri={}, status={}, payload={}, application={}",
                    MDC.get(Constant.REQUEST_ID_HEADER_NAME),
                    MDC.get(Constant.CORRELATION_ID_HEADER_NAME),
                    stopWatch.getLastTaskInfo().getTimeMillis(),
                    wrappedRequest.getMethod(),
                    wrappedRequest.getRequestURI(),
                    wrappedResponse.getStatus(),
                    IOUtils.toString(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding()),
                    this.name);


    }
}
