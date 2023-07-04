package com.chenjiacheng.teemo.protocol;

import com.sun.xml.internal.ws.api.pipe.Tube;
import lombok.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.chenjiacheng.teemo.protocol.HttpConstants.CRLF;

/**
 * create by chenjiacheng on 2023/7/4 04:54
 *
 * @author chenjiacheng
 * @since 1.0.0
 */
@Data
public class HttpResponse {
    private String version;
    private int code;
    private String msg;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;

    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append(version).append(" ");
        response.append(code).append(" ");
        response.append(msg);
        response.append(CRLF);

        headers.forEach((k, v) -> {
            response.append(k).append(": ").append(v).append(CRLF);
        });

        response.append(CRLF);

        response.append(body);
        return response.toString();
    }

    public ByteBuffer[] Bytebuffers() {
        StringBuilder response = new StringBuilder();
        response.append(version).append(" ");
        response.append(code).append(" ");
        response.append(msg);
        response.append(CRLF);

        headers.forEach((k, v) -> {
            response.append(k).append(": ").append(v).append(CRLF);
        });
        response.append(CRLF);
        return new ByteBuffer[]{ByteBuffer.wrap(response.toString().getBytes()),ByteBuffer.wrap(body)};
    }

    public static HttpResponse build(HttpRequest request) throws IOException {
        String path = request.getUrl();
        byte[] body = getBody(path);

        HttpResponse response = new HttpResponse();
        response.setVersion("HTTP/1.1");
        response.setCode(200);
        response.setMsg("OK");

        response.setBody(body);

        Map<String,String> headers = new HashMap<>();
        headers.put("Server","Teemo");
        headers.put("Content-Type",getMimeType(path));
        headers.put("Content-Length", String.valueOf(body.length));
        headers.put("Date", LocalDateTime.now().toString());
        // headers.put("accept-ranges","bytes");
        headers.put("Connection","close");
        //Connection: close

        response.setHeaders(headers);
        return response;
    }

    private static byte[] getBody(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    private static String getMimeType(String path) throws IOException {
        if(path.contains(".ico")){
            return "image/x-icon";
        }
        return Files.probeContentType(Paths.get(path));
    }

}
