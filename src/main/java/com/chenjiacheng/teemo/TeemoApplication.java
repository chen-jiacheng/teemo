package com.chenjiacheng.teemo;

import com.chenjiacheng.teemo.init.ConfigMonitorHandler;

import java.io.IOException;


/**
 * create by chenjiacheng on 2023/7/4 02:32
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
public class TeemoApplication {
    /**
     * Run the {@link TeemoApplication}.
     */
    public static void main(String[] args) throws IOException {
        new ConfigMonitorHandler("./teemo").start();
    }
}