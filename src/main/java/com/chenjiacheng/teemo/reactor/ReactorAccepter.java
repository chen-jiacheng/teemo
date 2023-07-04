package com.chenjiacheng.teemo.reactor;

import com.chenjiacheng.teemo.init.ServerConfig;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * create by chenjiacheng on 2023/7/4 02:32
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
public class ReactorAccepter {
    private Selector selector;
    private ServerConfig config;
    private static ExecutorService bossExecutor = Executors.newFixedThreadPool(5);


    public ReactorAccepter(ServerSocketChannel serverSocketChannel, ServerConfig config) throws IOException {
        this.selector = Selector.open();
        this.config = config;
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        bossExecutor.execute(()->{
            eventLoop();
        });
    }

    private void eventLoop(){
        try {
            while (true) {
                if (selector.select(500) == 0) {
                    continue;
                }
                Iterator<SelectionKey> acceptKeys = selector.selectedKeys().iterator();
                while (acceptKeys.hasNext()){
                    SelectionKey acceptKey = acceptKeys.next();
                    acceptKeys.remove();
                    if(acceptKey.isValid()&&acceptKey.isAcceptable()){
                        ServerSocketChannel serverChannel = (ServerSocketChannel)acceptKey.channel();
                        SocketChannel channel = serverChannel.accept();
                        System.out.println("接受新链接:"+channel.getRemoteAddress());
                        channel.configureBlocking(false);
                        new ReactorHandler(channel,config).start();
                    }
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

}
