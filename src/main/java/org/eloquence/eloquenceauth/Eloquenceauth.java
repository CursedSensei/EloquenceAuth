package org.eloquence.eloquenceauth;

import net.fabricmc.api.ModInitializer;
import org.eloquence.eloquenceauth.configs.ModConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Eloquenceauth implements ModInitializer {
    public static final String MOD_ID = "eloquenceauth";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
