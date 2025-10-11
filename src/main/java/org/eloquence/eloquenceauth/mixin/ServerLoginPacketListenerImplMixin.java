package org.eloquence.eloquenceauth.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {
    @Shadow
    public abstract void disconnect(Component component);

    @Inject(
            method = "verifyLoginAndFinishConnectionSetup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;disconnectAllPlayersWithProfile(Lcom/mojang/authlib/GameProfile;)Z"
            ),
            cancellable = true
    )
    private void onVerifyLoginAndFinishConnectionSetup(GameProfile gameProfile, CallbackInfo ci, @Local PlayerList playerList) {
        UUID uUID = gameProfile.getId();

        ServerPlayer serverPlayer2 = playerList.getPlayer(gameProfile.getId());
        if (serverPlayer2 != null) {
            this.disconnect(Component.literal("You are already in the server"));
            ci.cancel();
            return;
        }

        for (ServerPlayer serverPlayer : playerList.getPlayers()) {
            if (serverPlayer.getUUID().equals(uUID)) {
                this.disconnect(Component.literal("You are already in the server"));
                ci.cancel();
                return;
            }
        }
    }
}