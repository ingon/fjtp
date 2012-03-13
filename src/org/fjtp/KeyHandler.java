package org.fjtp;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class KeyHandler {
    public enum Ops {
        NONE(0),
        READ(SelectionKey.OP_READ),
        WRITE(SelectionKey.OP_WRITE),
        READ_WRITE(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        
        public final int ops;
        
        private Ops(int ops) {
            this.ops = ops;
        }
    }
    
    private SelectionKey key;
    
    public SelectionKey getKey() {
        return key;
    }
    
    public void setKey(SelectionKey key) {
        this.key = key;
    }
    
    public Ops read() throws IOException {
        return Ops.NONE;
    }
    
    public Ops write() throws IOException {
        return Ops.NONE;
    }

    protected SocketChannel getChannel() {
        return (SocketChannel) key.channel();
    }
}
