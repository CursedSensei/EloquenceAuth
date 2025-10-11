package org.eloquence.eloquenceauth;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.RandomSource;
import org.eloquence.eloquenceauth.configs.ModConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Eloquenceauth implements ModInitializer {
    public static final String MOD_ID = "eloquenceauth";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Map<UUID, Map<Integer, UUID>> twinMap = new HashMap<>();

    private static final RandomSource random = RandomSource.create();

    @Override
    public void onInitialize() {
        // Init config
        ModConfigs.registerConfigs();

//        DiscordAuth auth = new DiscordAuth(LOGGER);
//
//        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
//            auth.close();
//        });
    }
}
