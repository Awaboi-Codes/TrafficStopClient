package com.tcc.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tcc.client.TrafficStopClient;
import com.tcc.client.util.render.ESPRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.lwjgl.opengl.GL33;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class ESPMixin {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void onRenderLevel(CallbackInfo ci) {
        if (TrafficStopClient.isESP) {
            Minecraft mc = Minecraft.getInstance();
            PoseStack poseStack = new PoseStack();
            poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
            ESPRender.renderESP(poseStack);
        }
    }
}