package com.tsc.client.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class ClientMixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void onRenderLevel(CallbackInfo ci) {
    }
}