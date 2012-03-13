package org.fjtp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fjtp.http.HttpKeyHandler;
import org.fjtp.http.HttpKeyHandlerFactory;
import org.fjtp.http.HttpResourceHolder;
import org.fjtp.http.HttpResponseHolder;
import org.fjtp.http.HttpServerConfig;
import org.fjtp.http.servlet.HttpServletKeyHandler;
import org.fjtp.util.args.Args;
import org.fjtp.util.args.ArgsHandler;

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
                ctx.htdocs = value;
            }
        });
    }
    
    public static void main(String[] args) throws IOException {
        ARGS.process(args);
        
        HttpServerConfig conf = ARGS.ctx;
        conf.dynamicHandler = new HttpServletKeyHandler("/game", new HttpServlet() {
            protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
                Writer writer = res.getWriter();
                writer.append("Aba das ist fantastish!!!");
                writer.flush();
            }
        });
        
        System.out.println("Start Port: " + conf.port);
        System.out.println("Start Docs: " + conf.htdocs);
        
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
