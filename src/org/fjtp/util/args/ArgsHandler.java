package org.fjtp.util.args;

public interface ArgsHandler<T> {
    public void handle(T ctx, String value);
}
