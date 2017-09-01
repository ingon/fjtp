package org.fjtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.fjtp.http.*;
import org.fjtp.util.args.Args;
import org.fjtp.util.args.ArgsHandler;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class Main {
    public static final Args<HttpServerConfig> ARGS = new Args<HttpServerConfig>(new HttpServerConfig());
    static {
        ARGS.register("-port", new ArgsHandler<HttpServerConfig>() {
            public void handle(HttpServerConfig ctx, String value) {
                ctx.port = Integer.parseInt(value);
            }
        });
        ARGS.register("-htdocs", new ArgsHandler<HttpServerConfig>() {
            public void handle(HttpServerConfig ctx, String value) {
//                ctx.htdocs = value;
            }
        });
    }

    private static final ByteBuffer buffer;
    private static final HttpResponse response;
    static {
        StringBuilder sb = new StringBuilder();

        sb.append("HTTP/1.1 ").append("200").append(" ").append("OK").append("\r\n");
        sb.append("Server: fjtp").append("\r\n");
        sb.append("Content-Type: text/plain").append("\r\n");
        sb.append("Content-Length: 13").append("\r\n");
        sb.append("Date: Thu, 31 Aug 2017 15:13:00 GMT").append("\r\n");
//        sb.append("Connection: keep-alive").append("\r\n");
        sb.append("\r\n");
        sb.append("Hello, World!");

        String message = sb.toString();
        byte[] messageBytes = message.getBytes(US_ASCII);

        buffer = ByteBuffer.allocateDirect(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();

        response = new HttpResponse.LazyDuplicate(buffer);
    }
    
    public static void main(String[] args) throws IOException {
        ARGS.process(args);

        HttpServerConfig conf = ARGS.ctx;
//        conf.dynamicHandler = new HttpServletKeyHandler("/game", new HttpServlet() {
//            protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//                Writer writer = res.getWriter();
//                //writer.append("Aba das ist fantastish!!!");
//                writer.append("Hello, World!");
//                writer.flush();
//            }
//        });
        conf.handler = new HttpRequestHandler() {
            @Override
            public HttpResponse handle(HttpKeyHandler keyHandler, HttpRequest request) {
                return response;
            }
        };

//        conf.handler = new StaticHttpRequestHandler(conf.h)
        System.out.println("Start Port: " + conf.port);
//        System.out.println("Start Docs: " + conf.htdocs);

//        conf.keepAlive = false;
        final Server<HttpServerConfig, HttpKeyHandler> server = conf.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down...");
                server.stop();
            }
        });
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        br.readLine();
        
        server.stop();
    }
}
