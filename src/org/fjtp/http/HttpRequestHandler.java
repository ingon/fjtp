package org.fjtp.http;

public interface HttpRequestHandler {
    HttpResponse handle(HttpKeyHandler keyHandler, HttpRequest request);
}
