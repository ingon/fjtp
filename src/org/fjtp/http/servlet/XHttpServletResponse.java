package org.fjtp.http.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.fjtp.common.SDFHolder;

public class XHttpServletResponse implements HttpServletResponse {
    private static final Map<Integer, String> RESPONSE_CODE_MESSAGES = new HashMap<Integer, String>();
    static {
        RESPONSE_CODE_MESSAGES.put(200, "OK");
        RESPONSE_CODE_MESSAGES.put(302, "Found");
        RESPONSE_CODE_MESSAGES.put(304, "Not Modified");
        RESPONSE_CODE_MESSAGES.put(400, "Bad Request");
        RESPONSE_CODE_MESSAGES.put(404, "Not Found");
        RESPONSE_CODE_MESSAGES.put(500, "Internal Error");
    }
    
    protected int responseCode = 200;
    protected String responseMessage = "OK";
    
    protected Map<String, String> headers = new HashMap<String, String>();
    protected StringWriter writer;
    protected PrintWriter wrappedWriter;
    protected boolean finished;
    
    public XHttpServletResponse() {
    }
    
    public void addCookie(Cookie cookie) {
        String cookies = headers.get("Set-Cookie");
        if(cookies == null) 
            cookies = "";
        
        StringBuilder sb = new StringBuilder(cookies);
        sb.append(cookies);
        sb.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        
        if(cookie.getPath() != null)
            sb.append("Path=").append(cookie.getPath()).append(";");
        if(cookie.getMaxAge() > 0)
            sb.append("Expires=").append(SDFHolder.get().format(new Date(System.currentTimeMillis() + cookie.getMaxAge()*1000))).append(";");
        
        headers.put("Set-Cookie", sb.toString());
    }

    public void addDateHeader(String name, long date) {
        setDateHeader(name, date);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addIntHeader(String name, int value) {
        headers.put(name, String.valueOf(value));
    }

    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public String encodeRedirectURL(String arg0) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String encodeRedirectUrl(String arg0) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String encodeURL(String arg0) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String encodeUrl(String arg0) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void sendError(int code) throws IOException {
        setStatus(code, null);
    }

    public void sendError(int code, String msg) throws IOException {
        setStatus(code, msg);
    }

    public void sendRedirect(String location) throws IOException {
        setStatus(302, null);
        headers.put("Location", location);
    }

    public void setDateHeader(String name, long value) {
        headers.put(name, SDFHolder.get().format(value));
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setIntHeader(String name, int value) {
        headers.put(name, String.valueOf(value));
    }

    public void setStatus(int code) {
        setStatus(code, null);
    }

    public void setStatus(int code, String msg) {
        responseCode = code;
        responseMessage = msg != null ? msg : RESPONSE_CODE_MESSAGES.get(code);
    }

    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public int getBufferSize() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getCharacterEncoding() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getContentType() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Locale getLocale() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public PrintWriter getWriter() throws IOException {
        if(wrappedWriter == null) {
            writer = new StringWriter();
            wrappedWriter = new PrintWriter(writer);
        }
        return wrappedWriter;
    }

    public boolean isCommitted() {
        return finished;
    }

    public void reset() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void resetBuffer() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void setBufferSize(int arg0) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void setCharacterEncoding(String arg0) {
        // XXX Ignore
    }

    public void setContentLength(int len) {
        writer.getBuffer().setLength(len);
        headers.put("Content-Length", String.valueOf(len));
    }

    public void setContentType(String type) {
        headers.put("Content-Type", type);
    }

    public void setLocale(Locale arg0) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void finish() {
        if(finished)
            return;
        
        if(wrappedWriter != null)
            wrappedWriter.close();
        
        if(! containsHeader("Content-Length")) {
            int bufferLength = writer != null ? writer.getBuffer().length() : 0;
            headers.put("Content-Length", String.valueOf(bufferLength));
        }
        
        if(! containsHeader("Content-Type"))
            setContentType("text/plain");
        
        finished = true;
    }
}
