package org.fjtp.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.fjtp.common.MimeTypeMapper;
import org.fjtp.common.SDFHolder;

public class HttpResourceHolder {
    private static final Map<String, HttpResource> RESOURCES = new HashMap<String, HttpResource>();
    public static Set<String> ZIPPABLE = new HashSet<String>();
    
    static {
        ZIPPABLE.add("html");
        ZIPPABLE.add("js");
        ZIPPABLE.add("css");
    }
    
    public static HttpResource get(String resource) {
        return RESOURCES.get(resource);
    }
    
    public static void init(HttpServerConfig config, File base) throws IOException {
        init(config, base, "/");
        
        System.out.println("Files loaded");
    }

    private static void init(HttpServerConfig config, File base, String sbase) throws IOException {
        for(File f : base.listFiles()) {
            if(f.isDirectory()) {
                init(config, f, sbase + f.getName() + "/");
            } else {
                loadStaticFile(config, f, sbase);
            }
        }
    }

    private static void loadStaticFile(HttpServerConfig config, File f, String sbase) throws IOException {
        ByteBuffer finalBuffer = null;
        
        if(config.gzip && isGzippable(f)) {
            byte[] content = gzip(f);
            ByteBuffer headersBuffer = loadHeaders(config, f, content.length, true);
            
            finalBuffer = ByteBuffer.allocateDirect(headersBuffer.limit() + content.length);
            finalBuffer.put(headersBuffer);
            finalBuffer.put(content);
            finalBuffer.flip();
        } else {
            ByteBuffer contentBuffer = loadData(f);
            ByteBuffer headersBuffer = loadHeaders(config, f, contentBuffer.limit(), false);
            
            finalBuffer = ByteBuffer.allocateDirect(headersBuffer.limit() + contentBuffer.limit());
            finalBuffer.put(headersBuffer);
            finalBuffer.put(contentBuffer);
            finalBuffer.flip();
        }
        
        HttpResource sr = new HttpResource();
        sr.lastModified = f.lastModified();
        sr.data = finalBuffer;
        
        RESOURCES.put(sbase + f.getName(), sr);
    }
    
    private static ByteBuffer loadHeaders(HttpServerConfig config, File f, long length, boolean gzip) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK").append("\r\n");
        sb.append("Content-type: ").append(MimeTypeMapper.getType(f)).append("\r\n");
        sb.append("Content-Length: ").append(length).append("\r\n");
        sb.append("Last-Modified: ").append(SDFHolder.get().format(f.lastModified())).append("\r\n");
        sb.append("Expires: Wed, 29 Dec 2012 10:50:22 EET").append("\r\n");
        sb.append("Cache-Control: max-age=604800, public, max-age=604800").append("\r\n");
        if(gzip)
            sb.append("Content-Encoding: gzip").append("\r\n");
        
        if(config.keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");
        sb.append("\r\n");
        return HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
    }
    
    private static ByteBuffer loadData(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        try {
            return fc.map(MapMode.READ_ONLY, 0, f.length());
        } finally {
            fc.close();
        }
    }
    
    private static boolean isGzippable(File f) {
        return ZIPPABLE.contains(getExtension(f.getName()));
    }

    public static String getExtension(String s) {
        int index = s.lastIndexOf(".");
        return index > 0 ? s.substring(index + 1) : "";
    }
    
    private static byte[] gzip(File f) throws IOException {
        ByteArrayOutputStream fout = new ByteArrayOutputStream((int) f.length());
        GZIPOutputStream gos = new GZIPOutputStream(fout);
        writeContent(new FileInputStream(f), gos);
        gos.close();
        return fout.toByteArray();
    }

    private static void writeContent(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[4096];
        int bytesWritten = 0;
        int threshold = 4194304;
        try {
            for (int cnt = 0; (cnt = in.read(b)) > 0; ) {
                out.write(b, 0, cnt);
                bytesWritten += cnt;
                if (bytesWritten >= threshold) {
                    out.flush();
                    bytesWritten = 0;
                }
            }
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
            }
            try {
               out.close();
           } catch (IOException ex) {
           }
        }
    }
}
