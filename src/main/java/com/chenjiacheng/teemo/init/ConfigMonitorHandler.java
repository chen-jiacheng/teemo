package com.chenjiacheng.teemo.init;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * create by chenjiacheng on 2023/7/4 02:32
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
public class ConfigMonitorHandler {

    private String path;
    private ConfigProcessHandler configProcessHandler;

    public ConfigMonitorHandler(String path) throws IOException {
        this.path = path;
        this.configProcessHandler = new ConfigProcessHandler();
        init(path);
    }

    private void init(String path) throws IOException {
        Path root = Paths.get(path);
        checkWorkPath(root);
    }

    private void checkWorkPath(Path root) throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectories(root);
            Path config = root.resolve("default.server");
            String defaultConfigPath = this.getClass()
                    .getClassLoader()
                    .getResource("default.server")
                    .getPath();
            Path defaultConfig = Paths.get(defaultConfigPath);
            Files.copy(defaultConfig, config, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void start()throws IOException{
        Path root = Paths.get(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            FileSystem fs = FileSystems.getDefault();
            PathMatcher matcher = fs.getPathMatcher("regex:.*\\.server");
            for (Path file: stream) {
                if(!matcher.matches(file)){
                    continue;
                }
                System.out.println(file.getFileName()+"正在启动");
                this.configProcessHandler.process(file);
            }
        }
    }

    public void monitor() throws IOException {
        Path folder = Paths.get(path);

        WatchService watcher = FileSystems.getDefault().newWatchService();
        folder.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        System.out.println("Watch Service registered for dir: " + folder.getFileName());
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                System.out.println(kind.name() + ": " + fileName);

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    // 一个新的文件已经被创建
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    // 一个文件已经被删除
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    // 一个文件已经被修改
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

}
