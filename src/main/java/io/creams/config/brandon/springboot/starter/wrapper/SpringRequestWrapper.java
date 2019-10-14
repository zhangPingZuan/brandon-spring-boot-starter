package io.creams.config.brandon.springboot.starter.wrapper;


import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Snow
 * @version 1.0
 * @date 2019-09-16 17:24
 * @description
 */
public class SpringRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public SpringRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            body = IOUtils.toByteArray(request.getInputStream());
        } catch (IOException ex) {
            body = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ServletInputStream() {
            public boolean isFinished() {
                return false;
            }

            public boolean isReady() {
                return true;
            }

            public void setReadListener(ReadListener readListener) {
            }

            ByteArrayInputStream byteArray = new ByteArrayInputStream(body);

            @Override
            public int read() {
                return byteArray.read();
            }
        };
    }
}
