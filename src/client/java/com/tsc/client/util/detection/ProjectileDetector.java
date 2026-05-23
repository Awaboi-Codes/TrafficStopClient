package com.tsc.client.util.detection;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

public class ProjectileDetector {

    /**
     * Checks if an arrow fired by another entity is within 1 block of the player.
     * @return true if an enemy arrow is within 1 block, false otherwise.
     */
    public static boolean isArrowWithinOneBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;

        // Scan for all arrow entities within a 1.0 block expansion box around the player
        for (AbstractArrow arrow : mc.level.getEntitiesOfClass(AbstractArrow.class, mc.player.getBoundingBox().inflate(2))) {

            // 1. Ignore arrows stuck in walls/ground
            // 2. Ignore arrows that you shot yourself
            if (arrow.onGround() || arrow.getOwner() == mc.player) {
                continue;
            }

            // 3. Exact Euclidean distance check from arrow to player body
            double distance = mc.player.distanceTo(arrow);
            if (distance <= 2.0D) {
                return true; // Found an enemy arrow inside your 1-block bubble
            }
        }

        return false; // No threatening arrows nearby
    }
}
