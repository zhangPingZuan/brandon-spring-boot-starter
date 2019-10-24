
## 一、组件介绍
> brandon-spring-boot-starter（以下简称brandon）是用来打印每个流入应用的http请求和http消息，并且将格式化后的日志输送到logstash中。与springboot采用的日志实现不同，brandon采用log4j2作为日志实现，log4j2采用了disruptor技术，在性能方面上优于其他日志实现，笔者认为log4j2 + slf4j应该是未来的大势所趋。brandon运用java code configuration的风格融入到springboot的配置中。

## 二、使用说明以及日志信息各字段含义
#### 使用说明

```
    <groupId>io.creams.config</groupId>
    <artifactId>brandon-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
```
如果想要开启logstash的日志传输，可以在配置文件中添加以下配置：

```
creams.tool:
  brand:
    enabled: true
    logstash-host: 'localhost'
    logstash-port: 5000
```
#### 各字段含义
1. **X-Request-ID**: 每个请求或者消息的id。
2. **X-Correlation-ID**: 在微服务中，一个请求可以衍生出多个请求或者消息，X-Correlation-ID用于关联起一个请求及其衍生请求或者消息。
3. **application**: 该应用的名称，取自spring.application.name，默认不传则为-。
4. **http_response({} ms)**: 表示该请求的处理时间。

#### 效果图如下
![image](https://raw.githubusercontent.com/zhangPingZuan/brandon-spring-boot-starter/master/src/main/resources/static/image/image1.png)
正是通过brandon，笔者才知道原来我们的服务的接口竟然这么慢。。。。。。。。

## 三、使用java code风格配置log4j2以及优点（重点）
#### 笔者的无奈
- 一开始，笔者想直接使用Piotr Mińkowski写的logstash-logging-spring-boot-starter(https://github.com/piomin/spring-boot-logging), 该starter的效果与brandon一样，都是打印日志并且传输到logstash中。只不过它用的是logback，而不是log4j2。
- ok，故事就从这里开始吧。笔者的同事在项目中使用了sentry，sentry是一个监控平台，它会把项目中产生的exception日志传输到平台上通过钉钉提醒开发人员该服务出现异常。senty说巧不巧使用的是log4j2，想必大家都知道slf4j在项目中只允许一种日志实现的存在，要么log4j2，要么logback，你要是两个都用，就给你报错。
- 那么笔者要想引入brandon的功能，前提条件就是不能影响sentry的正常使用。于是笔者把目标转向基于log4j2的logstash-logging-spring-boot-starter改造实现，也就是brandon。

#### log4j2的java code配置
stackoverflow最常见的是xml方式的配置（基本上找不到优雅的java code配置），但是这种配置有以下缺点：
1. 由于log4j2的加载时间比springboot要早，读取写在bootstrap.yml或者applicaiton.yml的配置信息非常困难。
2. 如果项目中已经有其他的依赖使用log4j2.xml(比如sentry，它就已经把log4j2.xml占用了)，那么就需要使用到log4j2的compostite confuration技术。才能将设计的appender和原来的appender进行merge。
![image](https://raw.githubusercontent.com/zhangPingZuan/brandon-spring-boot-starter/master/src/main/resources/static/image/image5.png)

最后笔者写出的java code配置方式：
![image](https://raw.githubusercontent.com/zhangPingZuan/brandon-spring-boot-starter/master/src/main/resources/static/image/image2.png)

## 四、集成含有spring-cloud-stream组件时的坑以及解决方案

先来看这么一段话：
> This is normal. Spring Cloud Stream is used in order to setup the binding between your application and the message broker, in your case RabbitMQ.
Behind the scenes Spring Cloud Stream is creating a new context to create all the necessary beans for you so you can use those to send and receive messages from the broker.
When you see Fetching config from server at : for the second time, it's actually being logged from the second context which is created by Spring Cloud Stream.https://github.com/spring-cloud/spring-cloud-config/issues/1419

就是说项目中有spring-cloud-stream依赖，项目会生成两个applicationContext。而brandon没有生效是因为注册到了第一个applicationContext。

#### 解决方案
在applicaiotn context启动后再去加载brandon的配置
![image](https://raw.githubusercontent.com/zhangPingZuan/brandon-spring-boot-starter/master/src/main/resources/static/image/image3.png)

#### 效果图
![image](https://raw.githubusercontent.com/zhangPingZuan/brandon-spring-boot-starter/master/src/main/resources/static/image/image4.png)


