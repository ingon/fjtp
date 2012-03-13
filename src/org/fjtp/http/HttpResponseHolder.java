package org.fjtp.http;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class HttpResponseHolder {
    private static ByteBuffer notModified;
    private static ByteBuffer notFound;
    private static ByteBuffer badRequest;
    
    public static void init(HttpServerConfig config) {
        initNotModified(config);
        initNotFound(config);
        initBadRequest(config);
    }
    
    public static ByteBuffer getNotModified() {
        return notModified.duplicate();
    }
    
    public static ByteBuffer getBadRequest() {
        return badRequest.duplicate();
    }
    
    public static ByteBuffer getNotFound() {
        return notFound.duplicate();
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
        
        notModified = HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
    }
    
    private static void initNotFound(HttpServerConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 404 Not Found").append("\r\n");
        if(config.keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");
        sb.append("\r\n");
        
        notFound = HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
    }

    private static void initBadRequest(HttpServerConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 400 Bad Request").append("\r\n");
        if(config.keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");
        sb.append("\r\n");
        
        badRequest = HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
    }
}
