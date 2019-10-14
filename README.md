# brandon-spring-boot-starter

log every request and middle message and output to logstash in log4j2 other than logback

## for what
```
    1. 打印出每个http请求、http消息的日志，在header上添加上X-Request-ID、X-Correlation-ID，并且计算出每个请求的处理时间.
    2. 把日志输出到logstash。
```
    

#### X-Request-ID
```
    每个请求的唯一标识
```

#### X-Correlation-ID
```
    一次请求产生的相关http请求或者http消息共用的标识。
    举个例子： 合同审核通过， 
    oa请求 ---> 合同消息 ---> 账单消息 ---> 
    account消息 ---> assets消息 ---> data-stats消息 
    会有一个相同的X-Correlation-ID。
```

## note
```
    兼容sentry的logger。原作者采用springboot默认的日志实现logback。
    brandon采用log4j2，并且使用java code的形式进行配置。非常灵活
```

## 踩坑记录
```$xslt
    springboot 配合 spring-cloud-starter-stream-rabbit会生成两个applicationContext
    引入brandon后，Brandon会将logger配置到springboot默认applicationContext，不会配置到
    spring-cloud-starter-stream-rabbit中的applicationContext，导致logstash的appender失效。
    解决方案：
        利用ApplicationStartedEvent事件进行appender的配置。
        在应用中可以看到ApplicationStartedEvent事件处理了两次，从而证实
        springboot 配合 spring-cloud-starter-stream-rabbit会生成两个applicationContext。
```


## destination
```
    配合sentry 修复bug方便点。
```

## reference
```
    https://piotrminkowski.wordpress.com/2019/05/07/logging-with-spring-boot-and-elastic-stack/
```
