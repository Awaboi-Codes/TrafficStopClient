package com.tcc.client.util.packetspoof;

import com.tcc.client.util.packetspoof.PacketPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.List;

public class CombatUtil {

    private static boolean wasAttackPressed = false;
    private static final double MAX_REACH = 6.0D; // Standard survival reach limitation boundary

    /**
     * MODULE 2: Standard KillAura (Multi-Target Damage Dispatch)
     * Attacks surrounding mobs within range using default mechanical properties
     */
    public static void killAura() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        List<Entity> targets = mc.level.getEntitiesOfClass(Entity.class, mc.player.getBoundingBox().inflate(MAX_REACH));

        for (Entity entity : targets) {
            if (entity == mc.player || !(entity instanceof LivingEntity target) || !target.isAlive()) {
                continue;
            }

            if (mc.player.getEyePosition().distanceTo(target.position()) <= MAX_REACH) {
                // Pass standard values with zero extra packet displacement requests
                SpoofData profile = new SpoofData(target, 0.0D, false);
                parseAndSendPacket(profile);
            }
        }
    }

    public static void espRender() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        List<Entity> targets = mc.level.getEntitiesOfClass(Entity.class, mc.player.getBoundingBox().inflate(100));

        for (Entity entity : targets) {
            if (entity == mc.player || !(entity instanceof LivingEntity target) || !target.isAlive()) {
                continue;
            }


        }
    }

    /**
     * MODULE 3: Integrated Critical & KillAura Combiner
     * Loops over all nearby entities and bundles forced physics packets before each strike
     */
    public static void killAuraCriticalCombiner() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        List<Entity> targets = mc.level.getEntitiesOfClass(Entity.class, mc.player.getBoundingBox().inflate(MAX_REACH));

        for (Entity entity : targets) {
            if (entity == mc.player || !(entity instanceof LivingEntity target) || !target.isAlive()) {
                continue;
            }

            if (mc.player.getEyePosition().distanceTo(target.position()) <= MAX_REACH) {
                // If wielding a Mace, request custom displacement; else fallback to normal crits
                double customHeight = mc.player.getMainHandItem().is(Items.MACE) ? 2.5D : 0.0D;
                boolean wantCrits = (customHeight == 0.0D);

                SpoofData profile = new SpoofData(target, customHeight, wantCrits);
                parseAndSendPacket(profile);
            }
        }
    }

    /**
     * Instantiates the custom network payload object and processes the packet pipeline execution
     */
    public static void parseAndSendPacket(SpoofData data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null || data.target == null) return;

        // 1. Instantiate the newly configured custom networking packet payload object
        PacketPayload customPayload = new PacketPayload(
                data.target.getId(),
                data.heightOffset,
                data.forceCrit
        );

        // 2. Transmit your custom packet payload profile directly over the active server connection channel
        ClientPlayNetworking.send(customPayload);

        // 3. Process the local visual spoof adjustments exactly as before
        if (mc.player.onGround() && !mc.player.isInWater() && !mc.player.isPassenger()) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            boolean horizCollision = mc.player.horizontalCollision;

            if (customPayload.heightOffset() > 0) {
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + customPayload.heightOffset(), z, false, horizCollision));
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, horizCollision));
            } else if (customPayload.forceCrit()) {
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0525D, z, false, horizCollision));
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0015D, z, false, horizCollision));
            }
        }

        // 4. Send the attack validation sequence
        mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(data.target, mc.player.isShiftKeyDown()));
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    public static void tryFastUse() {
        Minecraft mc = Minecraft.getInstance();
        // Validation: Ensure player is valid, in a world, and actively using an item
        if (mc.player == null || mc.getConnection() == null || !mc.player.isUsingItem()) return;

        // Get the active item's use animation type
        ItemUseAnimation anim = mc.player.getUseItem().getUseAnimation();

        // Target items with a duration loop: Eating food, drinking potions, bowing, or blocking
        if (anim == ItemUseAnimation.EAT || anim == ItemUseAnimation.DRINK || anim == ItemUseAnimation.BOW) {

            // Vanilla use duration is usually 32 ticks. Sending 32-35 position packets
            // in a single frame tricks the server into instantly completing the action.
            int packetBurstSize = 35;

            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            boolean horizCollision = mc.player.horizontalCollision;
            boolean onGround = mc.player.onGround();

            for (int i = 0; i < packetBurstSize; i++) {
                // Send standard positions over and over in the exact same network tick frame
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                        x, y, z, onGround, horizCollision
                ));
            }

            // Client-side visual completion cleanup: Stops the arm rendering hand movement locally
            mc.player.stopUsingItem();
        }
    }
}
