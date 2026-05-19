package org.eloquence.eloquenceauth;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.eloquence.eloquenceauth.configs.ModConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EloquenceAuth implements ModInitializer {
    public static final String MOD_ID = "eloquenceauth";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final long AUTH_TIMEOUT_MS = 5000L;

    public static DiscordAuth auth;

    @Override
    public void onInitialize() {
        // Init config
        ModConfigs.registerConfigs();

        auth = new DiscordAuth(LOGGER);

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (auth != null) {
                auth.close();
            }
        });
    }

    public static boolean hasPlayer(GameProfile gameProfile, PlayerList playerList) {
        UUID uUID = gameProfile.getId();

        ServerPlayer serverPlayer2 = playerList.getPlayer(uUID);
        if (serverPlayer2 != null) {
            return true;
        }

        for (ServerPlayer serverPlayer : playerList.getPlayers()) {
            if (serverPlayer.getUUID().equals(uUID)) {
                return true;
            }
        }

        return false;
    }

    public static boolean authenticatePlayer(String playerName) {
        if (auth == null) {
            return false;
        }

        return auth.authenticate(playerName, AUTH_TIMEOUT_MS);
    }
}
