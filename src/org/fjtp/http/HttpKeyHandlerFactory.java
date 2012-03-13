package org.fjtp.http;

import org.fjtp.KeyHandlerFactory;
import org.fjtp.Server;

public class HttpKeyHandlerFactory implements KeyHandlerFactory<HttpServerConfig, HttpKeyHandler> {
    public HttpKeyHandler newInstance(Server<HttpServerConfig, HttpKeyHandler> server) {
        return new HttpKeyHandler(server);
    }
}
