package org.fjtp.http;

import java.io.File;
import java.io.IOException;

import org.fjtp.Server;
import org.fjtp.ServerConfig;

public class HttpServerConfig extends ServerConfig {
    public boolean keepAlive = true;
    public boolean gzip = false;
//    public String htdocs;
//    public HttpRequestHandler dynamicHandler = new HttpRequestHandler() {
//        public boolean handle(HttpKeyHandler keyHandler) {
//            return false;
//        }
//    };
    public HttpRequestHandler handler;
    
    public Server<HttpServerConfig, HttpKeyHandler> startServer() throws IOException {
        HttpResponseHolder.init(this);

        return new Server<HttpServerConfig, HttpKeyHandler>(this, new HttpKeyHandlerFactory());
    }
}
