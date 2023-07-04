package com.chenjiacheng.teemo.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * create by chenjiacheng on 2023/7/4 07:23
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
public class ReactorBootstrap {
    private ReactorAccepter boss;
    private ReactorHandler worker;
    private ServerSocketChannel serverChannel;

    private void group(ReactorAccepter boss, ReactorHandler worker) {
        this.boss = boss;
        this.worker = worker;
    }

    private void bind(InetSocketAddress addr) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.bind(addr);
        boss.registry(this);
        boss.start();
        worker.start();
    }


    public static class ReactorAccepter {
        private Selector selector;
        private int threads;
        private ExecutorService executor;
        private ReactorBootstrap bootstrap;

        public ReactorAccepter(int threads,ThreadFactory threadFactory) throws IOException {
            this.threads = threads > 0 ? threads : Runtime.getRuntime().availableProcessors() * 2;
            this.selector = Selector.open();
            this.executor = Executors.newFixedThreadPool(this.threads,threadFactory);
        }

        public void registry(ReactorBootstrap bootstrap) throws IOException {
            this.bootstrap = bootstrap;
            this.bootstrap.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }

        public void start() {
            executor.execute(() -> {
                try {
                    this.eventLoop();
                } catch (IOException e) {
                    System.out.println(Thread.currentThread().getName() + " -ReactorAccepter::start:" + e.getMessage());
                }
            });
        }

        private void eventLoop() throws IOException {
            while (true) {
                System.out.println(Thread.currentThread().getName() + "- ReactorAccepter::eventLoop");
                if (this.selector.select()==0) {
                    continue;
                }
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                while (selectionKeys.hasNext()) {
                    SelectionKey selectionKey = selectionKeys.next();
                    selectionKeys.remove();
                    if (selectionKey.isValid() && selectionKey.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel channel = serverChannel.accept();
                        channel.configureBlocking(false);
                        System.out.println("新连接: " + channel.getRemoteAddress());
                        this.bootstrap.worker.registry(channel);
                    }
                }
            }
        }
    }

    public static class ReactorHandler {
        private Selector selector;
        private int threads;
        private ExecutorService executor;

        public ReactorHandler(int threads,ThreadFactory threadFactory) throws IOException {
            this.selector = Selector.open();
            this.threads = threads > 0 ? threads : Runtime.getRuntime().availableProcessors() * 2;
            this.executor = Executors.newFixedThreadPool(threads,threadFactory);
        }

        public void registry(SocketChannel channel) throws ClosedChannelException {
            channel.register(selector, SelectionKey.OP_READ);
            selector.wakeup();
        }

        public void start() {
            for (int i = 0; i < threads; i++) {
                executor.execute(() -> {
                    try {
                        System.out.println("ReactorHandler::start");
                        eventLoop();
                    } catch (IOException e) {
                        System.out.println(Thread.currentThread().getName() + " -ReactorHandler::start:" + e.getMessage());
                    }
                });
            }
        }

        private synchronized void eventLoop() throws IOException {
            while (true) {
                System.out.println(Thread.currentThread().getName() + "- ReactorHandler::eventLoop");
                if (this.selector.select() == 0) {
                    continue;
                }
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                while (selectionKeys.hasNext()) {
                    SelectionKey selectionKey = selectionKeys.next();
                    selectionKeys.remove();
                    if (selectionKey.isValid() && selectionKey.isReadable()) {
                        read0(selectionKey);
                    } else if (selectionKey.isValid() && selectionKey.isWritable()) {
                        write0(selectionKey);
                    }
                }
            }
        }

        private void read0(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = channel.read(buffer);
            // read: -1:通道关闭 0:数据读取完成 >0: 读取的数据字节数
            if (read == -1) {
                channel.close();
                return;
            }
            String msg = new String(buffer.array()).trim();
            channel.write(new ByteBuffer[]{ByteBuffer.wrap("echo: ".getBytes()), buffer});
            System.out.println("收到消息: " + msg);
        }

        private void write0(SelectionKey key) {

        }


    }

    public static void main(String[] args) throws IOException {
        ReactorAccepter boss = new ReactorAccepter(1,new DefaultThreadFactory("boss"));
        ReactorHandler worker = new ReactorHandler(1,new DefaultThreadFactory("worker"));

        ReactorBootstrap bootstrap = new ReactorBootstrap();
        bootstrap.group(boss, worker);
        bootstrap.bind(new InetSocketAddress(8080));
    }

    public static class DefaultThreadFactory implements ThreadFactory {
        private String prefix;
        private AtomicInteger netId = new AtomicInteger();

        public DefaultThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            String threadName = prefix + "-" + netId.getAndIncrement();
            Thread thread = new Thread(r, threadName);
            thread.setDaemon(Thread.currentThread().getThreadGroup().isDaemon());
            return thread;
        }
    }


}


