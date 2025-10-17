package org.eloquence.eloquenceauth;

import org.eloquence.eloquenceauth.configs.ModConfigs;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;

public class DiscordAuth extends Thread {
    private final Logger LOGGER;
    private ServerSocket listener = null;
    private boolean running = true;

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
        while (running && listener == null) {
            try {
                listener = new ServerSocket(ModConfigs.DISCORD_AUTH_PORT, 50, InetAddress.getLoopbackAddress());
                break;
            } catch (IOException ignored) {
                LOGGER.error("Failed. Retrying to bind port: {} in 5 secs", ModConfigs.DISCORD_AUTH_PORT);
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
                return;
            }
        }

        LOGGER.info("Successfully bound to port: {}", ModConfigs.DISCORD_AUTH_PORT);

        Socket authClient;

        while (running) {
            LOGGER.info("Waiting for Auth Client connection");

            try {
                authClient = listener.accept();
            } catch (ClosedByInterruptException ignored) {
                break;
            } catch (IOException ignored) {
                continue;
            }

            // algo

            try {
                authClient.close();
            } catch (IOException ignored) {}
        }

        LOGGER.info("Closing DiscordAuth");
        try {
            listener.close();
        } catch (IOException ignored) {}
    }
}
