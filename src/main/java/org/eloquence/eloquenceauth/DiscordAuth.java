package org.eloquence.eloquenceauth;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordAuth extends Thread {
    private final Logger LOGGER;
    private SocketChannel client = null;
    private boolean running = true;
    private ConcurrentHashMap<String, AuthTicket> tickets = new ConcurrentHashMap<>();

    // And used as lock :P
    private static final Path SOCKET_PATH = Paths.get("DiscordAuth.sock");

    public DiscordAuth(Logger logger) {
        super("DiscordAuth Thread");
        this.setDaemon(true);
        LOGGER = logger;

        this.start();
    }

    public void close() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        ServerSocketChannel listener = null;

        while (running) {
            try {
                try {
                    Files.deleteIfExists(SOCKET_PATH);
                } catch (IOException e) {
                    LOGGER.error("Unable to delete unix socket file. Close any program using this file: {}", SOCKET_PATH);
                }

                UnixDomainSocketAddress address = UnixDomainSocketAddress.of(SOCKET_PATH);
                listener = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
                listener.bind(address);
                break;
            } catch (IOException ignored) {
                LOGGER.error("Failed. Retrying to bind to unix socket file");

                if (listener != null) {
                    try {
                        listener.close();
                    }
                    catch (IOException ignored1) { }
                    finally {
                        listener = null;
                    }
                }
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
                return;
            }
        }

        assert listener != null;

        LOGGER.info("Successfully listening to socket file");

        while (running) {
            LOGGER.info("Waiting for Auth Client connection");

            ByteBuffer buffer = ByteBuffer.allocate(90);

            synchronized (SOCKET_PATH) {
                try {
                    client = listener.accept();
                } catch (IOException ignored) { }
            }

            while (running && client.isConnected()) {
                buffer.clear();
                try {
                    client.read(buffer);
                } catch (IOException ignored) {
                    continue;
                }

                // read algo to threadsafe hashmap
            }

            synchronized (SOCKET_PATH) {
                try {
                    client.close();
                } catch (IOException ignored) { }

                client = null;
            }
        }

        LOGGER.info("Closing DiscordAuth");
        try {
            listener.close();
        } catch (IOException ignored) {}
    }
}
