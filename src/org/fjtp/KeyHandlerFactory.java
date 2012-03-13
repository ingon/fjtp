package org.fjtp;

public interface KeyHandlerFactory<SC extends ServerConfig, KH extends KeyHandler> {
    public KH newInstance(Server<SC, KH> server);
}
