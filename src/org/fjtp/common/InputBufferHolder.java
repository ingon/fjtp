package org.fjtp.common;

import java.nio.ByteBuffer;

public class InputBufferHolder {
    private static final int BUFFER_SIZE = 4096;
    
    private static final ThreadLocal<ByteBuffer> LOCAL = new ThreadLocal<ByteBuffer>() {
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocateDirect(BUFFER_SIZE);
        };
    };
    
    public static ByteBuffer get() {
        return LOCAL.get();
    }
}
