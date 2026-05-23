package com.tsc.client.mixin;

import com.tsc.client.TrafficStopClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class) // Single clean target that safely handles both methods on the client thread
public class ClientMixinLocalPlayer {

    @Shadow
    @Final
    protected Minecraft minecraft;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void injectMovementSpoof(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Minecraft client = Minecraft.getInstance();

        if (TrafficStopClient.isElytraBoosting) {
            player.setDeltaMovement(player.getHeadLookAngle().multiply(1.5, 1.5, 1.5));
        }



        // Only manipulate physics calculations if the cheat button was toggled ON
        if (TrafficStopClient.isFlying) {
            // 1. Force the engine to think you are stuck in a cobweb frame loop.
            player.horizontalCollision = true;

            // 2. Spoof player status vectors to simulate vertical resistance
            if (player.getDeltaMovement().y < 0) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(TrafficStopClient.velocityFlySpeed, -0.01, TrafficStopClient.velocityFlySpeed));
                player.setOnGround(false);
            }
        }

        if (TrafficStopClient.isBoatFly && player.isPassenger()) {

            // Check if the current ridden vehicle is a Boat instance
            if (player.getVehicle() instanceof Boat boat) {

                // 1. Grab current directional viewing angles from the player
                float yaw = player.getYRot();
                double radYaw = Math.toRadians(yaw);

                // 2. Set movement speed multipliers (adjust these to fly faster/slower)
                double horizontalSpeed = 1.2;
                double verticalSpeed = 0.0;

                // 3. Track keyboard movement inputs using client game options
                double forward = 0;
                double strafe = 0;

                if (client.options.keyUp.isDown()) forward += 1;
                if (client.options.keyDown.isDown()) forward -= 1;
                if (client.options.keyLeft.isDown()) strafe -= 1;
                if (client.options.keyRight.isDown()) strafe += 1;

                // 4. Calculate vertical movement using the Spacebar (jump) and Shift (crouch) keys
                if (client.options.keyJump.isDown()) {
                    verticalSpeed = 0.6;  // Fly upward
                } else if (client.options.keyShift.isDown()) {
                    verticalSpeed = -0.6; // Fly downward
                } else {
                    // Force vertical velocity to 0 to hover mid-air when no vertical keys are held
                    verticalSpeed = 0.0;

                    // Optional: Apply an incredibly tiny downward vector (e.g., -0.01)
                    // if you ever need to bypass specific server anti-cheat hover checks.
                }

                // 5. Use basic trigonometry to translate keyboard directional inputs to 3D world vectors
                double motionX = -(Math.sin(radYaw) * forward + Math.cos(radYaw) * strafe) * horizontalSpeed;
                double motionZ = (Math.cos(radYaw) * forward - Math.sin(radYaw) * strafe) * horizontalSpeed;
                double motionY = verticalSpeed;

                // 6. Completely override the boat's velocity vectors with our calculated tracking movement
                boat.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
                boat.setYRot(player.getYRot());
            }
        }

        if (TrafficStopClient.isProtectSelf && player.getLastAttacker() != null) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1).add(0, -0.1, 0));
            player.attack(player.getLastAttacker());
        }

        if (TrafficStopClient.isJesus) {
            net.minecraft.core.BlockPos feetPos = player.blockPosition();
            net.minecraft.world.level.block.state.BlockState feetState =
                    Minecraft.getInstance().level.getBlockState(feetPos);

            boolean inFluid = !feetState.getFluidState().isEmpty();

            if (inFluid) {
                player.setOnGround(true);
                double yVel = player.getDeltaMovement().y;
                if (yVel < 0) {
                    // Only cancel downward velocity, don't push up
                    player.setDeltaMovement(
                            player.getDeltaMovement().x,
                            0,
                            player.getDeltaMovement().z
                    );
                }
            }
        }
    }

    private double fallStartY = Double.MAX_VALUE;
    private boolean wasFalling = false;

    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void onSendPosition(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (TrafficStopClient.isNoFall && !player.isSpectator() && !player.isCreative()) {
            double currentY = player.getY();
            boolean isFalling = player.getDeltaMovement().y < 0;

            if (isFalling) {
                if (!wasFalling) {
                    fallStartY = currentY;
                    wasFalling = true;
                }

                double fallen = fallStartY - currentY;

                if (fallen >= 1.0) {
                    player.setOnGround(true);
                    // Force send the packet immediately with onGround=true
                    player.connection.send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly(true, false));
                    fallStartY = currentY;
                }
            } else {
                wasFalling = false;
                fallStartY = currentY;
            }
        }
    }
}
