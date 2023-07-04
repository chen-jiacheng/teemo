package com.chenjiacheng.teemo.init;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * create by chenjiacheng on 2023/7/4 02:32
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
@Data
public class ServerConfig {
    private String domain;
    private int port;
    private String root;
    private boolean gzip;
    private boolean keepalive;
    private String idle;

    public static ServerConfig fromFile(Path path) throws IOException {
        ServerConfig config = new ServerConfig();
        Files.lines(path).forEach(line -> {
            String[] parts = line.split(": ");
            switch (parts[0]) {
                case "domain":
                    config.setDomain(parts[1]);
                    break;
                case "port":
                    config.setPort(Integer.parseInt(parts[1]));
                    break;
                case "root":
                    config.setRoot(parts[1]);
                    break;
                case "gzip":
                    config.setGzip(parts[1].equalsIgnoreCase("on"));
                    break;
                case "keepalive":
                    config.setKeepalive(parts[1].equalsIgnoreCase("on"));
                    break;
                case "idle":
                    config.setIdle(parts[1]);
                    break;
            }
        });
        return config;
    }

}
