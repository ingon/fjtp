package org.fjtp.http;

import java.nio.ByteBuffer;

public interface HttpResponse {
    boolean isReady();

    ByteBuffer buffer();

    class Literal implements HttpResponse {
        protected final ByteBuffer buffer;

        public Literal(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public ByteBuffer buffer() {
            return buffer;
        }
    }

    class EagerDuplicate extends Literal {
        public EagerDuplicate(ByteBuffer buffer) {
            super(buffer.duplicate());
        }
    }

    class LazyDuplicate extends Literal {
        public LazyDuplicate(ByteBuffer buffer) {
            super(buffer);
        }

        @Override
        public ByteBuffer buffer() {
            return buffer.duplicate();
        }
    }
}