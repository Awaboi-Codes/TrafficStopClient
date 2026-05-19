package com.tcc.client.mixin;

import com.tcc.client.TrafficStopClient;
import net.minecraft.*;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void injectMovementSpoof(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        // Only manipulate physics calculations if the cheat button was toggled ON
        if (TrafficStopClient.isWebSpoofActive) {
            // 1. Force the engine to think you are stuck in a cobweb frame loop.
            // This drastically alters the server friction and vector parsing logic.
            player.horizontalCollision = true;

            // 2. Spoof player status vectors to simulate vertical resistance
            // (Tells modern anti-cheats your movement is caused by environmental friction)
            if (player.getDeltaMovement().y < 0) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(1.0, 0.05, 1.0));
            }
        }
    }
}
