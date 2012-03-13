package org.fjtp.common;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SDFHolder {
    private static final ThreadLocal<SimpleDateFormat> LOCAL = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        };
    };
    
    public static SimpleDateFormat get() {
        return LOCAL.get();
    }
}
