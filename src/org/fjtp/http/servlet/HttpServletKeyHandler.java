package org.fjtp.http.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.fjtp.http.*;
import org.fjtp.util.AbstractPooledExecutor;

public class HttpServletKeyHandler implements HttpRequestHandler {
    private static final HttpResponse LATER = new HttpResponse() {
        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public ByteBuffer buffer() {
            return null;
        }
    };

    public final String basePath;
    public final HttpServlet servlet;
    
    private final AbstractPooledExecutor<HttpKeyHandler> handlers = new AbstractPooledExecutor<HttpKeyHandler>() {
        protected void perform(HttpKeyHandler t) {
            ByteBuffer data = doServe(t.getConfig(), t.getRequest());
            t.respond(new HttpResponse.Literal(data));
        }
    };    
    
    public HttpServletKeyHandler(String basePath, HttpServlet servlet) {
        this.basePath = basePath;
        
        this.servlet = servlet;
        try {
            this.servlet.init();
        } catch (ServletException e) {
            throw new RuntimeException("Error on servlet init!", e);
        }
        
        handlers.start(10); // XXX property
    }

    @Override
    public HttpResponse handle(HttpKeyHandler keyHandler, HttpRequest request) {
        if(request.resource.startsWith(basePath)) {
            handlers.enqueue(keyHandler);
            return LATER;
        } else {
            return null;
        }
    }

    protected ByteBuffer doServe(HttpServerConfig config, HttpRequest request) {
        XHttpServletRequest srequest = new XHttpServletRequest(request);
        XHttpServletResponse sresponse = new XHttpServletResponse();
        
        try {
            servlet.service(srequest, sresponse);
        } catch (Throwable t) {
            try {
                sresponse.sendError(500);
            } catch (IOException e) {
            }  
        }
        
        sresponse.finish();
        ByteBuffer headersBuffer = headers(config, sresponse);
        ByteBuffer contentBuffer = content(config, sresponse);
        
        ByteBuffer finalBuffer = null;
        
        if(contentBuffer != null) {
            finalBuffer = ByteBuffer.allocateDirect(headersBuffer.limit() + contentBuffer.limit());
            finalBuffer.put(headersBuffer);
            finalBuffer.put(contentBuffer);
            finalBuffer.flip();
        } else {
            finalBuffer = headersBuffer;
        }
        
        return finalBuffer;
    }
    
    private ByteBuffer headers(HttpServerConfig config, XHttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("HTTP/1.1 ").append(response.responseCode).append(" ").append(response.responseMessage).append("\r\n");
        
        for(Map.Entry<String, String> header : response.headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        
        if(config.keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");
        sb.append("\r\n");
        
        return HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
    }
    
    private ByteBuffer content(HttpServerConfig config, XHttpServletResponse response) {
        if(response.writer == null)
            return null;
        
        return HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(response.writer.getBuffer()));
    }
}
