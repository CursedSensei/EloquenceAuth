package org.eloquence.eloquenceauth.configs;

import com.mojang.datafixers.util.Pair;

import static org.eloquence.eloquenceauth.EloquenceAuth.MOD_ID;

public class ModConfigs {
    public static SimpleConfig CONFIG;
    private static ModConfigProvider configs;

    public static int DISCORD_AUTH_PORT;

    public static void registerConfigs() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(MOD_ID).provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("discordAuthPort", 25566), "      Sets the TCP port to listen for Discord Auth");
    }

    private static void assignConfigs() {
        DISCORD_AUTH_PORT = CONFIG.getOrDefault("discordAuthPort", 25566);
    }
}