package org.fjtp.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.fjtp.http.HttpRequest;
import org.fjtp.util.StringUtils;

public class XHttpServletRequest implements HttpServletRequest {
    private static Cookie[] EMPTY_COOKIE = new Cookie[0];
    private final HttpRequest request;
    private String pathInfo;
    private Cookie[] cookies;
    
    XHttpServletRequest(HttpRequest request){
        this.request = request;
        int index = request.resource.indexOf("/", 1);
        if(index > 0)
            pathInfo = request.resource.substring(index);
        
        request.parseParams();
    }
    
    public String getAuthType() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Cookie[] getCookies() {
        if(cookies==null){
            cookies = EMPTY_COOKIE;
            String c = request.headers.get("Cookie");
            if(c!=null) {
                c = c.trim();
                String[] tokens = StringUtils.split(c, ';');
                cookies = new Cookie[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    String[] ts = StringUtils.split(tokens[i], '=');
                    cookies[i] = new Cookie(ts[0].trim(), ts[1]);
                }
            }
        }
        return cookies;
    }

    public long getDateHeader(String name) {
        Date d = request.getDateHeader(name);
        return d==null?-1:d.getTime();
    }

    public String getHeader(String name) {
        return request.headers.get(name);
    }

    public Enumeration getHeaders(String name) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Enumeration getHeaderNames() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public int getIntHeader(String name) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getMethod() {
        return request.method;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getPathTranslated() {
        return request.resource;
    }

    public String getContextPath() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getQueryString() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getRemoteUser() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getRequestedSessionId() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getRequestURI() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getServletPath() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public HttpSession getSession() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Object getAttribute(String name) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Enumeration getAttributeNames() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getCharacterEncoding() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        //ignore
    }

    public int getContentLength() {
        return request.getContentLength();
    }
    public String getContentType() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getParameter(String name) {
        String[] value = request.params.get(name);
        if(value == null || value.length == 0)
            return null;
        return value[0];
    }

    public Enumeration getParameterNames() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String[] getParameterValues(String name) {
        return request.params.get(name);
    }

    public Map getParameterMap() {
        return Collections.unmodifiableMap(request.params);
    }

    public String getProtocol() {
        return request.version;
    }

    public String getScheme() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getServerName() {
        return request.headers.get("Host").trim();
    }

    public int getServerPort() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getRemoteAddr() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getRemoteHost() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void setAttribute(String name, Object o) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public void removeAttribute(String name) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Locale getLocale() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public Enumeration getLocales() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public boolean isSecure() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getRealPath(String path) {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public int getRemotePort() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getLocalName() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public String getLocalAddr() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

    public int getLocalPort() {
        throw new UnsupportedOperationException(); //XXX implement me
    }

}
