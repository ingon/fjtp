package org.fjtp.http;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class HttpResponseHolder {
    private static HttpResponse notModified;
    private static HttpResponse notFound;
    private static HttpResponse badRequest;
    
    public static void init(HttpServerConfig config) {
        initNotModified(config);
        initNotFound(config);
        initBadRequest(config);
    }
    
    public static HttpResponse getNotModified() {
        return notModified;
    }
    
    public static HttpResponse getBadRequest() {
        return badRequest;
    }
    
    public static HttpResponse getNotFound() {
        return notFound;
    }
    
    private static void initNotModified(HttpServerConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 304 Not Modified").append("\r\n");
        sb.append("Expires: Wed, 29 Dec 2010 10:50:22 EET").append("\r\n");
        if(config.keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");
        sb.append("\r\n");
        
        ByteBuffer buffer = HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
        notModified = new HttpResource(0, buffer);
    }
    
    private static void initNotFound(HttpServerConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 404 Not Found").append("\r\n");
        if(config.keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");
        sb.append("\r\n");
        
        ByteBuffer buffer = HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
        notFound = new HttpResource(0, buffer);
    }

    private static void initBadRequest(HttpServerConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 400 Bad Request").append("\r\n");
        if(config.keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");
        sb.append("\r\n");
        
        ByteBuffer buffer = HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
        badRequest = new HttpResource(0, buffer);
    }
}
