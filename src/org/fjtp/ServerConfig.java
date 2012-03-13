package org.fjtp;

public abstract class ServerConfig {
    public String host = "localhost";
    public int port = 80;
    public int backlog = 1024;
    
    public int readerThreads = 5;
    public int writerThreads = 3;
    public int purgerThreads = 1;
}
