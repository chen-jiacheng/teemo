package com.chenjiacheng.teemo.reactor;

import com.chenjiacheng.teemo.protocol.HttpParser;
import com.chenjiacheng.teemo.init.ServerConfig;
import com.chenjiacheng.teemo.protocol.HttpRequest;
import com.chenjiacheng.teemo.protocol.HttpResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * create by chenjiacheng on 2023/7/4 02:32
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
public class ReactorHandler {

    private Selector selector;
    private ServerConfig config;
    private static ExecutorService workerExecutor = Executors.newFixedThreadPool(10);

    public ReactorHandler(SocketChannel channel, ServerConfig config) throws IOException {
        this.config = config;
        this.selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
    }

    public void start() throws IOException {
        workerExecutor.execute(()->{
            eventLoop();
        });
    }

    private void eventLoop() {
        try {
            while (true) {
                if (selector.select(500) == 0) {
                    continue;
                }
                System.out.println("轮询[R/W]事件中");
                Iterator<SelectionKey> acceptKeys = selector.selectedKeys().iterator();
                while (acceptKeys.hasNext()) {
                    SelectionKey key = acceptKeys.next();
                    SelectableChannel channel = key.channel();
                    if (!channel.isOpen()) {
                        System.out.println("通道是关闭的");
                        break;
                    }
                    acceptKeys.remove();
                    if (key.isValid() && key.isReadable()) {
                        read(key);
                    }
                    // else if (key.isValid() && key.isWritable()) {
                    //     write(key);
                    // }
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private void read(SelectionKey key) throws IOException {
        System.out.println("读取数据事件");
        SocketChannel channel = (SocketChannel) key.channel();
        if (!channel.isOpen()) {
            return;
        }
        HttpRequest request = HttpParser.parse(channel);
        System.out.println("HTTP请求:\n" + request);

        //key.interestOps(SelectionKey.OP_WRITE);
        key.attach(request);

        write(key);
    }

    private void write(SelectionKey key) throws IOException {
        System.out.println("写入数据事件");
        SocketChannel channel = (SocketChannel) key.channel();

        HttpRequest request = (HttpRequest) key.attachment();
        String url = this.config.getRoot() + request.getUri();
        if ("/".equals(request.getUri())) {
            url = this.config.getRoot() + "/index.html";
        }
        if (!Files.exists(Paths.get(url))) {
            // 3. 打印日志
            if ("/favicon.ico".equalsIgnoreCase(request.getUri())) {
                url = ReactorHandler.class.getClassLoader().getResource("favicon.ico").getFile();
            } else {
                url = ReactorHandler.class.getClassLoader().getResource("404.html").getFile();
            }
        }
        request.setUrl(url);
        System.out.println("url:" + request.getUrl());


        HttpResponse response = HttpResponse.build(request);
        channel.write(response.Bytebuffers());
        System.out.println("写入完成:"+request.getUri());

        //key.interestOps(SelectionKey.OP_READ);
        // channel.close();
    }


}
