# Teemo
> 取自LOL的一名约德尔人，迅捷斥候-提莫

> A simple Java NIO lightweight application

处理html，css，js，css 等静态资源。

技术：
    采用JavaNIO的API，
    使用Reactor模型-主从多线程模型

部署：
    java -jar teemo.jar

配置：./teemo/*.server

配置模板：
```txt
domain: chenjiacheng.com
port: 8080
root: ~/example
gzip: on
```
