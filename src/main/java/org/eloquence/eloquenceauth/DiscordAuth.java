package org.eloquence.eloquenceauth;

import org.eloquence.eloquenceauth.configs.ModConfigs;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

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
        while (running) {
            while (running && listener == null) {
                try {
                    listener = new ServerSocket(ModConfigs.DISCORD_AUTH_PORT);
                    break;
                } catch (IOException ignored) {}

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }

            LOGGER.info("Successfully binded to port: {}", ModConfigs.DISCORD_AUTH_PORT);

            // algo here
        }

        LOGGER.info("Closing Discord Auth");
    }
}
