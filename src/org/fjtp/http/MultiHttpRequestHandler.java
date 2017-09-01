package org.fjtp.http;

import java.util.List;

public class MultiHttpRequestHandler implements HttpRequestHandler {
    private final List<HttpRequestHandler> handlers;

    public MultiHttpRequestHandler(List<HttpRequestHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public HttpResponse handle(HttpKeyHandler keyHandler, HttpRequest request) {
        for (HttpRequestHandler handler : handlers) {
            HttpResponse result = handler.handle(keyHandler, request);
            if (result != null)
                return result;
        }
        return HttpResponseHolder.getNotFound();
    }
}
