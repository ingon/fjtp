package org.fjtp.http;

import org.fjtp.KeyHandler;
import org.fjtp.Server;
import org.fjtp.common.InputBufferHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class HttpKeyHandler extends KeyHandler {
    public static final Charset CHARSET = Charset.forName("UTF-8");

    private final Server<HttpServerConfig, HttpKeyHandler> server;
    private final HttpServerConfig config;
    private final HttpRequestHandler handler;
    
    private HttpRequest request = new HttpRequest();
    private HttpResponse response;
    
    public HttpKeyHandler(Server<HttpServerConfig, HttpKeyHandler> server) {
        this.server = server;
        this.config = server.getConfig();
        this.handler = config.handler;
    }

    public Ops read() throws IOException {
        ByteBuffer inputBuffer = InputBufferHolder.get();
        inputBuffer.clear();
        
        int bytesRead = getChannel().read(inputBuffer);
        if(bytesRead == 0)
            return Ops.READ;
        if(bytesRead < 0)
            return null;
        
        inputBuffer.flip();
        
        if(request.partialData(CHARSET.decode(inputBuffer))) {
            response = handler.handle(this, request);
            return response.isReady() ? Ops.WRITE : Ops.NONE;
        } else {
            return Ops.READ;
        }
    }
    
    public Ops write() throws IOException {
        ByteBuffer buffer = response.buffer();

        getChannel().write(buffer);
        if(buffer.remaining() > 0)
            return Ops.WRITE;
        
        if(config.keepAlive) {
            request = new HttpRequest();
            response = null;
            return Ops.READ;
        } else {
            return null;
        }
    }

    public HttpServerConfig getConfig() {
        return config;
    }
    
    public HttpRequest getRequest() {
        return request;
    }

    public void respond(HttpResponse response) {
        this.response = response;
        if (response.isReady())
            server.enqueUpdate(getKey(), Ops.WRITE);
    }
}
