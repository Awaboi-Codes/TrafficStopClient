package com.tsc.client;

import com.tcc.tscmain;
import com.tsc.client.util.detection.ProjectileDetector;
import com.tsc.client.util.packetspoof.CombatUtil;
import com.tsc.client.util.packetspoof.PacketPayload;

import com.mojang.blaze3d.platform.InputConstants;
import com.tsc.client.util.packetspoof.SpoofData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;

public class TrafficStopClient implements ClientModInitializer {

	// 1. Persistent fields for HUD and configuration settings
	private static KeyMapping openHudKey;
	private static KeyMapping elytraBoostKey;

	// Movement Booleans
	public static boolean isFlying = false;
	public static double velocityFlySpeed = 1;
	public static boolean isElytraFly = false;
	public static boolean isNoFall = false;
	public static boolean isElytraBoost = false;
	public static boolean isElytraBoosting = false;
	public static boolean isBoatFly = false;
	public static boolean isStrafe = false;
	public static boolean isJesus = false;
	public static boolean isQuickElytraTakeoff = false;
	private static boolean wasUsingFirework = false;
	private static boolean takeoffPending = false;

	// Render Booleans
	public static boolean isPlayerESP = false;
	public static boolean isChestESP = false;
	public static boolean isBaseESP = false;
	public static boolean isXray = false;

	// Combat Booleans
	public static boolean isCriticals = false;
	public static boolean isProtectSelf = false;
	public static boolean isKillAura = false;
	public static boolean isEnderman = false;

	// Miscellaneous Booleans
	public static boolean isFastUse = false;
	public static boolean isBridge = false;

	// Griefing Booleans
	public static boolean isTrenchBot = false;

	Minecraft mc = Minecraft.getInstance();
	private static boolean wasAttackPressed = false;

	public static final SoundEvent MOD_TOGGLE = SoundEvent.createVariableRangeEvent(
			Identifier.fromNamespaceAndPath("tsc-client", "mod_toggle"));

	@Override
	public void onInitializeClient() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, Identifier.fromNamespaceAndPath("tsc-client", "mod_toggle"),
				SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("tsc-client", "mod_toggle")));

		// 3. Register the HUD initialization key binding mapping configuration (Default: 'M' key)
		openHudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"Traffic Stop Client - Menu Open",          // Localized translation key for the Options menu
				InputConstants.Type.KEYSYM,          // Specifies that this maps to a peripheral keyboard sensor
				GLFW.GLFW_KEY_RIGHT_SHIFT,                     // Default bound key on launch
				KeyMapping.Category.MISC             // Categorizes it in the game's menu layout under 'Miscellaneous'
		));

		elytraBoostKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"Traffic Stop Client - Elytra Boost Key",          // Localized translation key for the Options menu
				InputConstants.Type.KEYSYM,          // Specifies that this maps to a peripheral keyboard sensor
				GLFW.GLFW_KEY_LEFT_SHIFT,                     // Default bound key on launch
				KeyMapping.Category.MISC             // Categorizes it in the game's menu layout under 'Miscellaneous'
		));

		PayloadTypeRegistry.playC2S().register(PacketPayload.TYPE, PacketPayload.CODEC);


		// 5. Register the single consolidated loop handler targeting the end of every individual client loop tick
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			SpoofData spoofedPacket = new SpoofData(null, 0.0D, false);
			boolean isAttackPressed = mc.options.keyAttack.isDown();
			// --- MODULE A: ADMINISTRATION HUD SELECTION CHECKS ---
			while (openHudKey.consumeClick()) {
				if (client.player != null) {
					client.setScreen(new ModHUD(Component.literal("IdHud")));
				}
			}


			if (mc.player != null && mc.options.keyUse.isDown()) {
				tscmain.LOGGER.info("flySpeed: " + TrafficStopClient.velocityFlySpeed);
			}

			// Check if your NoFall checkbox/boolean is enabled from your ModHUD
			if (isElytraBoost && elytraBoostKey.isDown() && client.player.isFallFlying()) {
				isElytraBoosting = true;
			} else {
				isElytraBoosting = false;
			}

			if (isQuickElytraTakeoff && client.player != null) {
				boolean wantsToGlide = false;

				ItemStack mainHand = client.player.getMainHandItem();
				boolean usingFirework = client.player.isUsingItem() && mainHand.is(Items.FIREWORK_ROCKET);
				if (elytraBoostKey.isDown() && client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) wantsToGlide = true;
				if (usingFirework && !wasUsingFirework) wantsToGlide = true;
				wasUsingFirework = usingFirework;

				if (wantsToGlide && !client.player.isFallFlying()) {
					if (client.player.onGround()) {
						// Jump first, schedule glide for next tick
						client.player.jumpFromGround();
						takeoffPending = true;
					}
				}

				if (takeoffPending && !client.player.onGround() && !client.player.isFallFlying()) {
					client.player.connection.send(new ServerboundPlayerCommandPacket(
							client.player,
							ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
					));
					takeoffPending = false;
				}
			}

			if (isKillAura) {
				CombatUtil.killAura();
			}

			if (TrafficStopClient.isElytraFly && client.player != null) {
				// Corrected check: if the player is NOT wearing an elytra, skip the code
				if (client.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
					// 1. Force the server-side flight packet if not already flying
					if (!client.player.isFallFlying()) {
						client.player.connection.send(new ServerboundPlayerCommandPacket(
								client.player,
								ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
						));
					}

					// 2. Grab current directional viewing angles from the player
					float yaw = client.player.getYRot();
					double radYaw = Math.toRadians(yaw);

					// 3. Set movement speed multipliers
					double horizontalSpeed = 1.6;
					double verticalSpeed = 0.0;

					// 4. Track keyboard movement inputs using client game options
					double forward = 0;
					double strafe = 0;

					if (client.options.keyUp.isDown()) forward += 1;
					if (client.options.keyDown.isDown()) forward -= 1;
					if (client.options.keyLeft.isDown()) strafe -= 1;
					if (client.options.keyRight.isDown()) strafe += 1;

					// 5. Calculate vertical movement using Spacebar and Shift keys
					if (client.options.keyJump.isDown()) {
						verticalSpeed = 0.5;
					} else if (client.options.keyShift.isDown()) {
						verticalSpeed = -0.5;
					} else {
						// Tiny negative value prevents the server from canceling elytra flight status
						verticalSpeed = -0.01;
					}

					// 6. Normalize diagonal vectors
					if (forward != 0 && strafe != 0) {
						forward *= 0.7071;
						strafe *= 0.7071;
					}

					// 7. Translate keyboard inputs to 3D world vectors
					double motionX = -(Math.sin(radYaw) * forward + Math.cos(radYaw) * strafe) * horizontalSpeed;
					double motionZ = (Math.cos(radYaw) * forward - Math.sin(radYaw) * strafe) * horizontalSpeed;
					double motionY = verticalSpeed;

					// 8. Override velocity
					client.player.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
				}
			}

			if (TrafficStopClient.isBridge && client.player != null) {
				if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
					if (mc.options.keyUse.isDown()) {
						// No block hit, place under player
						BlockPos pos = client.player.blockPosition().below();
						BlockState state = client.level.getBlockState(pos);
						if (state.isAir()) {
							client.gameMode.useItemOn(
									client.player,
									InteractionHand.MAIN_HAND,
									new BlockHitResult(
											Vec3.atCenterOf(pos),
											Direction.UP,
											pos,
											false
									)
							);
						}
					}
				}
			}

			if (TrafficStopClient.isStrafe && client.player != null) {
				// 1. Grab current directional viewing angles from the player
				float yaw = client.player.getYRot();
				double radYaw = Math.toRadians(yaw);

				// 2. Set movement speed multipliers
				double horizontalSpeed = 0.5;

				// 3. Track keyboard movement inputs using client game options
				double forward = 0;
				double strafe = 0;

				if (client.options.keyUp.isDown()) forward += 1;
				if (client.options.keyDown.isDown()) forward -= 1;
				if (client.options.keyLeft.isDown()) strafe -= 1;
				if (client.options.keyRight.isDown()) strafe += 1;

				// 4. Normalize vector math if moving diagonally to prevent moving faster sideways
				if (forward != 0 && strafe != 0) {
					forward *= 0.7071;
					strafe *= 0.7071;
				}

				// 5. Translate keyboard inputs to 3D world vectors
				double motionX = -(Math.sin(radYaw) * forward + Math.cos(radYaw) * strafe) * horizontalSpeed;
				double motionZ = (Math.cos(radYaw) * forward - Math.sin(radYaw) * strafe) * horizontalSpeed;

				// 6. Keep the player's existing Y velocity so falling, jumping, and gravity still function properly
				double motionY = client.player.getDeltaMovement().y;

				// 7. Override velocity cleanly without breaking game physics
				client.player.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
			}

			if (isCriticals) {

				// Only execute on the exact frame the user clicks (prevents machine-gun packet streaming)
				if (isAttackPressed && !wasAttackPressed) {
					if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
						EntityHitResult entityHit = (EntityHitResult) mc.hitResult;

						// Configure your packet parameters
						spoofedPacket.target = entityHit.getEntity();
						spoofedPacket.forceCrit = true;

						// Execute your sender pipeline here
						CombatUtil.parseAndSendPacket(spoofedPacket);

						// Optional: Release the key bind to prevent vanilla from duplicating the strike
						mc.options.keyAttack.setDown(false);
					}
				}
				// Update state tracking for the next tick frame
				wasAttackPressed = isAttackPressed;
			}

			if (isFastUse) {
				CombatUtil.tryFastUse();
			}
				// Perform active physics adjustments if toggled on and player isn't in Spectator Mode
				if (isFlying && client.player != null && !client.player.isSpectator()) {
					// Evaluates raw peripheral inputs directly out of the game engine's core controller maps
					boolean jump = client.options.keyJump.isDown();
					boolean sneak = client.options.keyShift.isDown();

					double ySpeed = jump ? 0.4D : (sneak ? -0.4D : 0.0D);

					// Read structural W/S (Forward) and A/D (Strafe) float multipliers (-1.0 to 1.0)
					net.minecraft.world.phys.Vec2 moveVector = client.player.input.getMoveVector();

					float strafeInput = -moveVector.x;   // Represents left / right strafe weight
					float forwardInput = moveVector.y;  // Represents forward / backward weight

					// Calculate direction angles relative to where the player entity is facing horizontally
					float yaw = client.player.yHeadRot;
					double radYaw = Math.toRadians(yaw);

					// Compute absolute X and Z vector movements based on directional trigonometry matrices
					double motionX = (forwardInput * -Math.sin(radYaw) + strafeInput * -Math.cos(radYaw)) * velocityFlySpeed;
					double motionZ = (forwardInput * Math.cos(radYaw) + strafeInput * -Math.sin(radYaw)) * velocityFlySpeed;

					// Force the player's underlying mechanical velocity attributes to match direction vectors
					client.player.setDeltaMovement(motionX, ySpeed, motionZ);
				}

				if (isEnderman && ProjectileDetector.isArrowWithinOneBlock()) {
					if (mc.player != null) {
						// 1. Generate a random launch speed between 5.0 and 10.0
						double speed = java.util.concurrent.ThreadLocalRandom.current().nextDouble(5.0, 10.0);

						// 2. Generate a random 360-degree angle in radians
						double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);

						// 3. Calculate horizontal velocity vectors (X and Z)
						double velocityX = Math.cos(angle) * velocityFlySpeed;
						double velocityZ = Math.sin(angle) * velocityFlySpeed;

						// 4. Set the new velocity vector (keeping vertical Y velocity intact or slightly lifted)
						mc.player.setDeltaMovement(velocityX, 0.1D, velocityZ);
					}
				}
				CombatUtil.parseAndSendPacket(spoofedPacket);
		});
	}
}
