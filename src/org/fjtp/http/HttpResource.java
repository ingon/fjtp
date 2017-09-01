package org.fjtp.http;

import java.nio.ByteBuffer;

public class HttpResource implements HttpResponse {
    public final long lastModified;
    public final ByteBuffer data;

    public HttpResource(long lastModified, ByteBuffer data) {
        this.lastModified = lastModified;
        this.data = data;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public ByteBuffer buffer() {
        return data.duplicate();
    }
}
