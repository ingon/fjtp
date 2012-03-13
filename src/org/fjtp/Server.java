package org.fjtp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.fjtp.KeyHandler.Ops;
import org.fjtp.util.AbstractPooledExecutor;
import org.fjtp.util.Pair;

public class Server<SC extends ServerConfig, KH extends KeyHandler> implements Runnable {
    private final SC config;
    private final KeyHandlerFactory<SC, KH> factory;
    
    private final Thread connectionThread;
    
    private ServerSocketChannel serverChannel;
    private Selector selector;
    
    private Queue<Pair<SelectionKey, Ops>> keysUpdates = new ConcurrentLinkedQueue<Pair<SelectionKey, Ops>>();
    
    private AbstractPooledExecutor<SelectionKey> readers = new AbstractPooledExecutor<SelectionKey>() {
        protected void perform(SelectionKey key) {
            KeyHandler handler = (KeyHandler) key.attachment();
            Ops ops = null;
            try {
                ops = handler.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            enqueUpdate(key, ops);
        }
    };

    private AbstractPooledExecutor<SelectionKey> writers = new AbstractPooledExecutor<SelectionKey>() {
        protected void perform(SelectionKey key) {
            KeyHandler handler = (KeyHandler) key.attachment();
            Ops ops = null;
            try {
                ops = handler.write();
            } catch (IOException e) {
                e.printStackTrace();
            }
            enqueUpdate(key, ops);
        }
    };
    
    private AbstractPooledExecutor<SelectableChannel> purger = new AbstractPooledExecutor<SelectableChannel>() {
        protected void perform(SelectableChannel channel) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    
    public Server(SC config, KeyHandlerFactory<SC, KH> factory) throws IOException {
        this.config = config;
        this.factory = factory;
        
        selector = SelectorProvider.provider().openSelector();
        
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(config.host, config.port), config.backlog);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        connectionThread = new Thread(this);
        connectionThread.start();
        
        readers.start(config.readerThreads);
        writers.start(config.writerThreads);
        purger.start(config.purgerThreads);
    }

    public SC getConfig() {
        return config;
    }
    
    public void stop() {
        connectionThread.interrupt();
        readers.shutdown();
        writers.shutdown();
        purger.shutdown();
    }
    
    private void register(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(false);
        KeyHandler handler = factory.newInstance(this);
        SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ, handler);
        handler.setKey(key);
    }
    
    private void processInterestUpdates() throws IOException {
        while(! keysUpdates.isEmpty()) {
            final Pair<SelectionKey, Ops> p = keysUpdates.poll();
            if(! p.f.isValid()) {
                continue;
            }
            
            if(p.s == null) {
                p.f.cancel();
                purger.enqueue(p.f.channel());
                continue;
            }
            
            p.f.interestOps(p.s.ops); 
        }
    }
    
    private void shutdown() throws IOException {
        selector.close();
        serverChannel.close();
    }

    public void run() {
        while(! Thread.interrupted()) {
            try {
                processInterestUpdates();
                processInterests();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void processInterests() throws IOException {
        int howMany = selector.select(100);
        if(howMany == 0)
            return;
        
        for(Iterator<SelectionKey> ite = selector.selectedKeys().iterator(); ite.hasNext(); ) {
            final SelectionKey key = ite.next();
            ite.remove();
            
            if(! key.isValid())
                continue;
            
            if(key.isAcceptable()) {
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                while(key.isAcceptable()) {
                    SocketChannel socket = channel.accept();
                    if(socket == null)
                        break;
                    register(socket);
                }
                continue;
            }
            
            key.interestOps(0);
            
            if(key.isReadable()) {
                readers.enqueue(key);
            }
            
            if(key.isWritable()) {
                writers.enqueue(key);
            }
        }
    }
    
    public void enqueUpdate(SelectionKey key, Ops ops) {
        keysUpdates.offer(Pair.of(key, ops));
        selector.wakeup();
    }
}
