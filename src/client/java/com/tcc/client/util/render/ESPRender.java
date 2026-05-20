package com.tcc.client.util.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL33;

public class ESPRender {

    public static void renderESP(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        try {
            GL33.glDisable(GL33.GL_DEPTH_TEST);
            GL33.glEnable(GL33.GL_BLEND);
            GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

            double camX = mc.gameRenderer.getMainCamera().position().x;
            double camY = mc.gameRenderer.getMainCamera().position().y;
            double camZ = mc.gameRenderer.getMainCamera().position().z;

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) continue;
                float r = 1.0f, g = 0.0f, b = 0.0f;
                if (entity instanceof Player) {
                    r = 1.0f; g = 0.35f; b = 0.0f;
                }
                AABB box = entity.getBoundingBox();
                drawBox(poseStack,
                        box.minX - camX, box.minY - camY, box.minZ - camZ,
                        box.maxX - camX, box.maxY - camY, box.maxZ - camZ,
                        r, g, b, 1.0f);
            }
        } finally {
            GL33.glEnable(GL33.GL_DEPTH_TEST);
            GL33.glDepthFunc(GL33.GL_LEQUAL);
            GL33.glDisable(GL33.GL_BLEND);
        }
    }

    private static void drawBox(PoseStack poseStack,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                float r, float g, float b, float a) {
        Matrix4f m = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.begin(
                VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Bottom
        line(buf, m, x1,y1,z1, x2,y1,z1, r,g,b,a);
        line(buf, m, x2,y1,z1, x2,y1,z2, r,g,b,a);
        line(buf, m, x2,y1,z2, x1,y1,z2, r,g,b,a);
        line(buf, m, x1,y1,z2, x1,y1,z1, r,g,b,a);
        // Top
        line(buf, m, x1,y2,z1, x2,y2,z1, r,g,b,a);
        line(buf, m, x2,y2,z1, x2,y2,z2, r,g,b,a);
        line(buf, m, x2,y2,z2, x1,y2,z2, r,g,b,a);
        line(buf, m, x1,y2,z2, x1,y2,z1, r,g,b,a);
        // Verticals
        line(buf, m, x1,y1,z1, x1,y2,z1, r,g,b,a);
        line(buf, m, x2,y1,z1, x2,y2,z1, r,g,b,a);
        line(buf, m, x2,y1,z2, x2,y2,z2, r,g,b,a);
        line(buf, m, x1,y1,z2, x1,y2,z2, r,g,b,a);

        MeshData mesh = buf.buildOrThrow();
        // Upload and draw using RenderPass
        // Need to find what replaced drawWithShader
    }

    private static void line(BufferBuilder buf, Matrix4f m,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float r, float g, float b, float a) {
        buf.addVertex(m, (float)x1, (float)y1, (float)z1).setColor(r, g, b, a);
        buf.addVertex(m, (float)x2, (float)y2, (float)z2).setColor(r, g, b, a);
    }
}