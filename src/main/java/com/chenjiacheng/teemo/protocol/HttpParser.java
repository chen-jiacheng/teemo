package com.chenjiacheng.teemo.protocol;

import com.chenjiacheng.teemo.protocol.HttpRequest;

import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.channels.*;

public class HttpParser {
    public static HttpRequest parse(SocketChannel socketChannel) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(buffer);
        if (read != -1) {
            buffer.flip();
            String msg = StandardCharsets.UTF_8.decode(buffer).toString();
            return parse(msg);
        }
        throw new RuntimeException("Channel is closed");
    }

    public static HttpRequest parse(String request) {
        HttpRequest httpRequest = new HttpRequest();

        // Split HTTP headers from body
        String[] requestParts = request.split("\r\n\r\n", 2);
        String headerPart = requestParts[0];
        String bodyPart = requestParts.length > 1 ? requestParts[1] : null;

        // Parse request line
        String[] lines = headerPart.split("\r\n");
        String requestLine = lines[0];
        String[] requestLineParts = requestLine.split(" ");
        httpRequest.setMethod(requestLineParts[0]);
        httpRequest.setUri(requestLineParts[1]);
        httpRequest.setVersion(requestLineParts[2]);

        // Parse headers
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] headerParts = lines[i].split(": ", 2);
            headers.put(headerParts[0], headerParts[1]);
        }
        httpRequest.setHeaders(headers);

        // Set body
        httpRequest.setBody(bodyPart);

        return httpRequest;
    }
}
