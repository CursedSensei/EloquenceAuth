package org.eloquence.eloquenceauth.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.eloquence.eloquenceauth.EloquenceAuth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.SocketAddress;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
    @Redirect(
            method = "verifyLoginAndFinishConnectionSetup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component onVerifyLoginAndFinishConnectionSetup(PlayerList playerList, SocketAddress socketAddress, GameProfile gameProfile) {
        Component component = playerList.canPlayerLogin(socketAddress, gameProfile);

        if (component == null) {
            if (EloquenceAuth.hasPlayer(gameProfile, playerList)) {
                component = Component.literal("You are already in the server");
            }
        }

        return component;
    }
}