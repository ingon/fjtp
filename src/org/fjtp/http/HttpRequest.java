package org.fjtp.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fjtp.common.SDFHolder;
import org.fjtp.util.StringUtils;

public class HttpRequest {
    public String method;
    public String resource;
    public String query;
    public String version;
    
    public Map<String, String> headers = new HashMap<String, String>();
    public Map<String, String[]> params = new HashMap<String, String[]>();
    
    public String message;
    
    private static enum ParseState {
        METHOD {
            public ParseState parse(HttpRequest req, ParseContext ctx) {
                int npos = ctx.sb.indexOf(" ", ctx.position);
                if(npos < 0)
                    return METHOD;
                req.method = ctx.sb.substring(ctx.position, npos);
                ctx.position = npos + 1;
                return RESOURCE.parse(req, ctx);
            }
        },
        RESOURCE {
            public ParseState parse(HttpRequest req, ParseContext ctx) {
                int npos = ctx.sb.indexOf(" ", ctx.position);
                if(npos < 0)
                    return RESOURCE;
                
                req.resource = ctx.sb.substring(ctx.position, npos);
                int qindex = req.resource.indexOf('?');
                if(qindex >= 0) {
                    req.query = req.resource.substring(qindex + 1);
                    req.resource = req.resource.substring(0, qindex);
                }
                
                ctx.position = npos + 1;
                return VERSION.parse(req, ctx);
            }
        },
        VERSION {
            public ParseState parse(HttpRequest req, ParseContext ctx) {
                int npos = ctx.sb.indexOf("\r\n", ctx.position);
                if(npos < 0)
                    return VERSION;
                req.version = ctx.sb.substring(ctx.position, npos);
                ctx.position = npos + 2;
                return HEADER.parse(req, ctx);
            }
        },
        HEADER {
            public ParseState parse(HttpRequest req, ParseContext ctx) {
                if(ctx.sb.charAt(ctx.position) == '\r') {
                    ctx.position += 2;
                    if (req.headers.containsKey("Content-Length")) {
                        return MESSAGE.parse(req, ctx);
                    } else {
                        return null;
                    }
                }
                int npos = ctx.sb.indexOf(":", ctx.position);
                if(npos < 0)
                    return HEADER;
                ctx.header = ctx.sb.substring(ctx.position, npos);
                ctx.position = npos + 1;
                return VALUE.parse(req, ctx);
            }
        },
        VALUE {
            public ParseState parse(HttpRequest req, ParseContext ctx) {
                int npos = ctx.sb.indexOf("\r\n", ctx.position);
                if(npos < 0)
                    return VALUE;
                String value = ctx.sb.substring(ctx.position, npos);
                req.headers.put(ctx.header, value);
                ctx.position = npos + 2;
                return HEADER.parse(req, ctx);
            }
        },
        MESSAGE {
            public ParseState parse(HttpRequest req, ParseContext ctx) {
                int msgLength = Integer.parseInt(req.headers.get("Content-Length").trim());
                if(ctx.sb.length() < ctx.position + msgLength) {
                    return MESSAGE;
                }
                
                req.message = ctx.sb.substring(ctx.position, ctx.position + msgLength);
                return null;
            }
        };
        
        public abstract ParseState parse(HttpRequest req, ParseContext ctx);
    }
    
    private static class ParseContext {
        protected final StringBuilder sb;
        protected int position = 0;
        protected String header;
        
        public ParseContext(CharSequence seq) {
            sb = new StringBuilder(seq);
        }
    }
    
    private ParseState state = ParseState.METHOD;
    private ParseContext ctx;
    
    protected boolean partialData(CharSequence data) {
        if(ctx == null) 
            ctx = new ParseContext(data);
        else
            ctx.sb.append(data);
        
        state = state.parse(this, ctx);
        
        return state == null;
    }
    
    public int getContentLength() {
        String contentLength = headers.get("Content-Length");
        if(contentLength == null || contentLength.trim().length() == 0) {
            return -1;
        }
        return Integer.parseInt(contentLength.trim());
    }

    public void parseParams() {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        if(query != null)
            parseParamsString(params, query);
        if("POST".equals(method))
            parseParamsString(params, message);
        
        for (Map.Entry<String, List<String>> e : params.entrySet()) {
            this.params.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
        }
    }
    
    private void parseParamsString(Map<String, List<String>> params, String str) {
        String data = urlDecode(str);
        if(StringUtils.isEmpty(data))
            return;
        
        int start = 0;
        for(int end = data.indexOf('&', start); end != -1; start = end + 1, end = data.indexOf('&', start)) {
            parseParamsPartial(params, data, start, end);
        }
        if(start >= 0) {
            parseParamsPartial(params, data, start, data.length());
        }
    }
    
    private void parseParamsPartial(Map<String, List<String>> params, String data, int start, int end) {
        int eqIndex = data.indexOf("=", start);
        if(eqIndex == -1 || eqIndex > end) {
            throw new IllegalArgumentException("Illegal params format");
        }
        
        String key = data.substring(start, eqIndex);
        String value = data.substring(eqIndex + 1, end);
        
        List<String> values = params.get(key);
        if(values == null) {
            values = new ArrayList<String>();
            params.put(key, values);
        }
        values.add(value);
    }
    
    private String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    public Date getDateHeader(String key) {
        String header = headers.get(key);
        if(header == null)
            return null;
        
        try {
            return SDFHolder.get().parse(header.trim());
        } catch (ParseException ex) {
            throw new IllegalArgumentException("404");
        }
    }

//    public static void main(String[] args) {
//        StringBuilder data = new StringBuilder(256);
//        data.append("GET /zazaza.html?x=y&z=&a=b HTTP/1.1\r\n");
//        data.append("Host: localhost\r\n");
//        data.append("User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13\r\n");
//        data.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n");
//        data.append("Accept-Language: en-us,en;q=0.5\r\n");
//        data.append("Accept-Encoding: gzip,deflate\r\n");
//        data.append("Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\n");
//        data.append("Keep-Alive: 115\r\n");
//        data.append("Connection: keep-alive\r\n");
//        data.append("Cache-Control: max-age=0\r\n");
//        data.append("\r\n");
//        
//        HttpRequest req = new HttpRequest();
//        req.partialData(data);
//        req.parseParams();
//        System.out.println("Data: " + req.params);
//        
//        for (int j = 0; j < 10; j++) {
//            int ite = 100000;
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < ite; i++) {
//                new HttpRequest().partialData(data);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println(ite + " iterations in " + (end - start) + "ms");
//        }
//    }
}
