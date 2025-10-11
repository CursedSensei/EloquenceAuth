package org.eloquence.eloquenceauth.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "disconnectAllPlayersWithProfile", at = @At("HEAD"), cancellable = true)
    private void onDisconnectAllPlayersWithProfile(GameProfile gameProfile, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
