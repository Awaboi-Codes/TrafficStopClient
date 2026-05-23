package com.tcc.client.mixin;

import com.tcc.client.TrafficStopClient;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class ClientMixinPlayer {
    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void cancelElytraPose(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Only override if your hack is on and you actually have an elytra equipped
        if (TrafficStopClient.isElytraFly && player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            player.setPose(Pose.STANDING);
            ci.cancel(); // Stop the game from forcing Pose.FALL_FLYING
        }
    }
}
