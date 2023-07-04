package com.chenjiacheng.teemo.init;

import com.chenjiacheng.teemo.reactor.ReactorServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * create by chenjiacheng on 2023/7/4 02:32
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
public class ConfigProcessHandler {

    public void process(Path config)throws IOException {
        //1. 读取配置文件
        ServerConfig serverConfig = ServerConfig.fromFile(config);

        //2. 启动ReactorServer
        new ReactorServer(serverConfig).start();



    }
}
