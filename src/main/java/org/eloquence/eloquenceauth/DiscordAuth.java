package org.eloquence.eloquenceauth;

import org.eloquence.eloquenceauth.configs.ModConfigs;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordAuth extends Thread {
    private final Logger LOGGER;
    private final Object clientLock = new Object();
    private Socket client = null;
    private BufferedWriter clientWriter = null;
    private Thread clientReaderThread = null;
    private ServerSocket listener = null;
    private volatile boolean running = true;
    private final ConcurrentHashMap<String, AuthTicket> tickets = new ConcurrentHashMap<>();

    public DiscordAuth(Logger logger) {
        super("DiscordAuth Thread");
        this.setDaemon(true);
        LOGGER = logger;

        this.start();
    }

    public void close() {
        running = false;
        closeQuietly(listener);
        synchronized (clientLock) {
            closeClientLocked();
        }
        this.interrupt();
    }

    @Override
    public void run() {
        try {
            listener = new ServerSocket(ModConfigs.DISCORD_AUTH_PORT);
            listener.setReuseAddress(true);
            LOGGER.info("Listening for Discord auth TCP client on port {}", ModConfigs.DISCORD_AUTH_PORT);

            while (running) {
                Socket incoming = listener.accept();

                synchronized (clientLock) {
                    if (hasActiveClientLocked()) {
                        closeQuietly(incoming);
                        continue;
                    }

                    client = incoming;
                    clientWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                    startClientReaderLocked(client);
                    LOGGER.info("Discord auth client connected from {}", client.getRemoteSocketAddress());
                }
            }
        } catch (IOException e) {
            if (running) {
                LOGGER.error("Discord auth TCP server stopped unexpectedly", e);
            }
        } finally {
            closeQuietly(listener);
            listener = null;
            synchronized (clientLock) {
                closeClientLocked();
            }
        }
    }

    public boolean authenticate(String name, long timeoutMs) {
        AuthTicket ticket = new AuthTicket(name);
        tickets.put(name, ticket);

        try {
            if (!sendNewUserName(name)) {
                return false;
            }

            synchronized (ticket) {
                if (!ticket.complete) {
                    try {
                        ticket.wait(timeoutMs);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            return ticket.complete && ticket.isAuthenticated;
        } finally {
            tickets.remove(name);
        }
    }

    private boolean sendNewUserName(String name) {
        synchronized (clientLock) {
            if (!hasActiveClientLocked() || clientWriter == null) {
                return false;
            }

            try {
                clientWriter.write(name);
                clientWriter.newLine();
                clientWriter.flush();
                return true;
            } catch (IOException e) {
                closeClientLocked();
                return false;
            }
        }
    }

    private void startClientReaderLocked(Socket socket) {
        clientReaderThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    handleAuthReply(line.trim());
                }
            } catch (IOException ignored) {
            } finally {
                synchronized (clientLock) {
                    closeClientLocked();
                }
            }
        }, "DiscordAuth Client Reader");
        clientReaderThread.setDaemon(true);
        clientReaderThread.start();
    }

    private void handleAuthReply(String line) {
        if (line.isEmpty()) {
            return;
        }

        boolean authenticated = true;
        String name = line;

        if (line.startsWith("ALLOW ")) {
            name = line.substring("ALLOW ".length()).trim();
        } else if (line.startsWith("DENY ")) {
            name = line.substring("DENY ".length()).trim();
            authenticated = false;
        }

        if (name.isEmpty()) {
            return;
        }

        AuthTicket ticket = tickets.get(name);
        if (ticket == null) {
            return;
        }

        synchronized (ticket) {
            ticket.isAuthenticated = authenticated;
            ticket.complete = true;
            ticket.notifyAll();
        }
    }

    private boolean hasActiveClientLocked() {
        return client != null && client.isConnected() && !client.isClosed();
    }

    private void closeClientLocked() {
        closeQuietly(client);
        closeQuietly(clientWriter);
        client = null;
        clientWriter = null;
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

}
