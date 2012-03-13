package org.fjtp.http;

import java.io.File;
import java.io.IOException;

import org.fjtp.Server;
import org.fjtp.ServerConfig;

public class HttpServerConfig extends ServerConfig {
    public boolean keepAlive = true;
    public boolean gzip = false;
    public String htdocs;
    public HttpDynamicKeyHandler dynamicHandler = new HttpDynamicKeyHandler() {
        public boolean handle(HttpKeyHandler keyHandler) {
            return false;
        }
    };
    
    public Server<HttpServerConfig, HttpKeyHandler> startServer() throws IOException {
        HttpResponseHolder.init(this);
        HttpResourceHolder.init(this, new File(this.htdocs));
        
        return new Server<HttpServerConfig, HttpKeyHandler>(this, new HttpKeyHandlerFactory());
    }
}
