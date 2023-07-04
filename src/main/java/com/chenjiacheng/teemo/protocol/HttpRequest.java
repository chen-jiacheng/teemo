package com.chenjiacheng.teemo.protocol;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * create by chenjiacheng on 2023/7/4 04:54
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
@Data
public class HttpRequest {
    private String method;
    private String uri;
    private String version;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private String url;
}
