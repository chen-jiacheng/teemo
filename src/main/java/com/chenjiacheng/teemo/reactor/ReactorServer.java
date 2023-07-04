package com.chenjiacheng.teemo.reactor;

import com.chenjiacheng.teemo.init.ServerConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * create by chenjiacheng on 2023/7/4 02:32
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
public class ReactorServer {

    private ServerConfig serverConfig;
    private ServerSocketChannel serverSocketChannel;

    private ExecutorService exec = Executors.newFixedThreadPool(10);


    public ReactorServer(ServerConfig serverConfig) throws IOException {
        this.serverConfig = serverConfig;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.bind(new InetSocketAddress(serverConfig.getDomain(),serverConfig.getPort()));
    }

    public void start()throws IOException {
        System.out.println(serverConfig.getDomain()+" ReactorServer启动中");
        this.exec.execute(()->{
            try {
                new ReactorAccepter(serverSocketChannel,this.serverConfig).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
