package org.fjtp.util.args;

import java.util.HashMap;
import java.util.Map;

public class Args<T> {
    public final T ctx;
    public final Map<String, ArgsHandler<T>> handlers = new HashMap<String, ArgsHandler<T>>();
    
    public Args(T ctx) {
        this.ctx = ctx;
    }
    
    public void register(String key, ArgsHandler<T> handler) {
        handlers.put(key, handler);
    }
    
    public T process(String[] args) {
        for(int i = 0; i < args.length; i+=2) {
            ArgsHandler<T> handler = handlers.get(args[i]);
            if(handler == null) {
                throw new RuntimeException("Unknown argument " + args[i] + " at " + i);
            }
            handler.handle(ctx, args[i + 1]);
        }
        return ctx;
    }
}
