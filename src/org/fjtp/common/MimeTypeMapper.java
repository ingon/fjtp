package org.fjtp.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeMapper {
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>();
    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("js", "text/javascript");
        MIME_TYPES.put("css", "text/css");
        
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("ico", "image/vnd.microsoft.icon");
    }
    
    public static String getType(File f) {
        return getType(f.getName());
    }

    public static String getType(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "text/plain" : MIME_TYPES.get(name.substring(index + 1));
    }
}
