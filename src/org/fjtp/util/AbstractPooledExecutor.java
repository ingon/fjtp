package org.fjtp.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractPooledExecutor<T> {
    protected final BlockingQueue<T> queue = new LinkedBlockingQueue<T>();
    private Thread[] threads;
    
    public AbstractPooledExecutor() {
    }
    
    public void start(int sz) {
        threads = new Thread[sz];
        for(int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                public void run() {
                    while(! isInterrupted()) {
                        try {
                            perform(queue.take());
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            };
            threads[i].start();
        }
    }
    
    public void shutdown() {
        for(Thread t : threads) {
            t.interrupt();
        }
    }
    
    protected abstract void perform(T t);
    
    public void enqueue(T t) {
        try {
            queue.put(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
