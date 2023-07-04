# teemo
取自LOL的一名约德尔人，迅捷斥候-提莫。

本项目是一款静态文件服务器。
处理，html，css，js，css 等静态资源。

技术：
    采用JavaNIO的API，
    使用Reactor模型-主从模型

部署：
    java -jar teemo.jar

配置：
    ~/.teemo.conf

文件内容：
```txt
server:{
    domain: chenjiacheng.com
    port: 8080
    root: ~/example
    gzip: on
    keepalive: on
    idle: 100s
}
server:{
    domain: chenjiacheng.com
    port: 8080
    root: ~/example
    gzip: on
    keepalive: on
    idle: 100s
}
```