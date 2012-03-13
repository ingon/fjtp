package org.fjtp.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.fjtp.KeyHandler;
import org.fjtp.Server;
import org.fjtp.common.InputBufferHolder;
import org.fjtp.common.SDFHolder;

public class HttpKeyHandler extends KeyHandler {
    public static final Charset CHARSET = Charset.forName("UTF-8");

    private static final Set<String> STATIC_MAPPING = new TreeSet<String>();
    static {
        STATIC_MAPPING.add("html");
        STATIC_MAPPING.add("js");
        STATIC_MAPPING.add("css");
        STATIC_MAPPING.add("png");
        STATIC_MAPPING.add("jpg");
        STATIC_MAPPING.add("ico");
    }
    
    private final Server<HttpServerConfig, HttpKeyHandler> server;
    private final HttpServerConfig config;
    
    private HttpRequest request = new HttpRequest();
    private ByteBuffer outBuffer;
    
    public HttpKeyHandler(Server<HttpServerConfig, HttpKeyHandler> server) {
        this.server = server;
        this.config = server.getConfig();
    }

    public Ops read() throws IOException {
        ByteBuffer inputBuffer = InputBufferHolder.get();
        inputBuffer.clear();
        
        int bytesRead = getChannel().read(inputBuffer);
        if(bytesRead == 0)
            return Ops.READ;
        if(bytesRead < 0)
            return null;
        
        inputBuffer.flip();
        
        if(request.partialData(CHARSET.decode(inputBuffer))) {
            if(config.dynamicHandler.handle(this)) {
                return Ops.NONE;
            } else {
                if(request.resource.endsWith("/")) {
                    request.resource += "index.html";
                }
                return Ops.WRITE;
            }

//            int dotIndex = request.resource.lastIndexOf('.');
//            if(dotIndex == -1) {
//                return Ops.NONE;
//            }
//            
//            String type = request.resource.substring(dotIndex + 1);
//            if(STATIC_MAPPING.contains(type)) {
//                return Ops.WRITE;
//            } else {
//                return Ops.NONE;
//            }
        } else {
            return Ops.READ;
        }
    }
    
    public Ops write() throws IOException {
        if(outBuffer == null)
            prepareOutBuffer();
        
        getChannel().write(outBuffer);
        if(outBuffer.remaining() > 0)
            return Ops.WRITE;
        
        if(config.keepAlive) {
            request = new HttpRequest();
            outBuffer = null;
            return Ops.READ;
        } else {
            return null;
        }
    }

    public HttpServerConfig getConfig() {
        return config;
    }
    
    public HttpRequest getRequest() {
        return request;
    }
    
    public void changeInterest(Ops ops) {
        server.enqueUpdate(this.getKey(), ops);
    }
    
    public void setOutBuffer(ByteBuffer outBuffer) {
        this.outBuffer = outBuffer;
    }
    
    private void prepareOutBuffer() throws IOException {
        HttpResource sr = HttpResourceHolder.get(request.resource);
        if(sr != null) {
            String ifModifiedStr = request.headers.get("If-Modified-Since");
            if (ifModifiedStr != null) {
                try {
                    Date ifModif = SDFHolder.get().parse(ifModifiedStr.trim());
                    if (sr.lastModified - ifModif.getTime() < 1000) {
                        outBuffer = HttpResponseHolder.getNotModified();
                    }
                } catch (ParseException e) {
                    outBuffer = HttpResponseHolder.getBadRequest();
                }
            }
            
            if(outBuffer == null)
                outBuffer = sr.data.duplicate();
        } else {
            outBuffer = HttpResponseHolder.getNotFound();
        }
    }
}
