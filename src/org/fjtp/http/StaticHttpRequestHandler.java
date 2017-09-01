package org.fjtp.http;

import org.fjtp.common.MimeTypeMapper;
import org.fjtp.common.SDFHolder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class StaticHttpRequestHandler implements HttpRequestHandler {
    private static final Set<String> ZIPPABLE = new HashSet<>();
    static {
        ZIPPABLE.add("html");
        ZIPPABLE.add("js");
        ZIPPABLE.add("css");
    }

    private final String root;
    private final boolean applyGZip;
    private final boolean keepAlive;

    private final Map<String, HttpResource> resources = new HashMap<>();

    public StaticHttpRequestHandler(String root, boolean applyGZip) throws IOException {
        this.root = root;
        this.applyGZip = applyGZip;
        this.keepAlive = true;

        init();
    }

    @Override
    public HttpResponse handle(HttpKeyHandler keyHandler, HttpRequest request) {
        String resource = request.resource;
        if(resource.endsWith("/")) {
            resource += "index.html";
        }

        HttpResource sr = resources.get(resource);
        if(sr == null) {
            return HttpResponseHolder.getNotFound();
        }

        String ifModifiedStr = request.headers.get("If-Modified-Since");
        if (ifModifiedStr != null) {
            try {
                Date ifModified = SDFHolder.get().parse(ifModifiedStr.trim());
                if (sr.lastModified - ifModified.getTime() < 1000) {
                    return HttpResponseHolder.getNotModified();
                }
            } catch (ParseException e) {
                return HttpResponseHolder.getBadRequest();
            }
        }

        return sr;
    }

    private void init() throws IOException {
        init(new File(root), "/");

        System.out.println("Files loaded");
    }

    private void init(File base, String sbase) throws IOException {
        for(File f : base.listFiles()) {
            if(f.isDirectory()) {
                init(f, sbase + f.getName() + "/");
            } else {
                loadStaticFile(f, sbase);
            }
        }
    }

    private void loadStaticFile(File f, String sbase) throws IOException {
        ByteBuffer finalBuffer = null;

        if(applyGZip && isGzippable(f)) {
            byte[] content = gzip(f);
            ByteBuffer headersBuffer = loadHeaders(f, content.length, true);

            finalBuffer = ByteBuffer.allocateDirect(headersBuffer.limit() + content.length);
            finalBuffer.put(headersBuffer);
            finalBuffer.put(content);
            finalBuffer.flip();
        } else {
            ByteBuffer contentBuffer = loadData(f);
            ByteBuffer headersBuffer = loadHeaders(f, contentBuffer.limit(), false);

            finalBuffer = ByteBuffer.allocateDirect(headersBuffer.limit() + contentBuffer.limit());
            finalBuffer.put(headersBuffer);
            finalBuffer.put(contentBuffer);
            finalBuffer.flip();
        }

        resources.put(sbase + f.getName(), new HttpResource(f.lastModified(), finalBuffer));
    }

    private ByteBuffer loadHeaders(File f, long length, boolean gzip) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK").append("\r\n");
        sb.append("Content-type: ").append(MimeTypeMapper.getType(f)).append("\r\n");
        sb.append("Content-Length: ").append(length).append("\r\n");
        sb.append("Last-Modified: ").append(SDFHolder.get().format(f.lastModified())).append("\r\n");
        sb.append("Expires: Wed, 29 Dec 2012 10:50:22 EET").append("\r\n");
        sb.append("Cache-Control: max-age=604800, public, max-age=604800").append("\r\n");
        if(gzip)
            sb.append("Content-Encoding: applyGZip").append("\r\n");

        if(keepAlive)
            sb.append("Connection: keep-alive").append("\r\n");
        else
            sb.append("Connection: close").append("\r\n");

        sb.append("\r\n");
        return HttpKeyHandler.CHARSET.encode(CharBuffer.wrap(sb));
    }

    private ByteBuffer loadData(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        try {
            return fc.map(FileChannel.MapMode.READ_ONLY, 0, f.length());
        } finally {
            fc.close();
        }
    }

    private boolean isGzippable(File f) {
        return ZIPPABLE.contains(getExtension(f.getName()));
    }

    public String getExtension(String s) {
        int index = s.lastIndexOf(".");
        return index > 0 ? s.substring(index + 1) : "";
    }

    private byte[] gzip(File f) throws IOException {
        ByteArrayOutputStream fout = new ByteArrayOutputStream((int) f.length());
        GZIPOutputStream gos = new GZIPOutputStream(fout);
        writeContent(new FileInputStream(f), gos);
        gos.close();
        return fout.toByteArray();
    }

    private void writeContent(InputStream in, OutputStream out) throws IOException {
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
