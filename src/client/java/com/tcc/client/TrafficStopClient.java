package com.tcc.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.mixin.client.keybinding.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayerResolver;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TrafficStopClient implements ClientModInitializer {

	// 1. Persistent fields for HUD and configuration settings
	private static KeyMapping openHudKey;
	private static KeyMapping elytraBoostKey;

	// Movement Booleans
	public static boolean isFlying = false;
	public static boolean isJumpHackActive = false;
	public static boolean isNoFallActive = false;
	public static boolean isElytraBoost = false;
	public static boolean isElytraBoosting = false;
	public static boolean isBoatFly = false;

	// Render Booleans
	public static boolean isESP = false;
	public static boolean isXray = false;

	// Combat Booleans
	public static boolean isCriticals = false;
	public static boolean isProtectSelf = false;

	@Override
	public void onInitializeClient() {
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

		// 5. Register the single consolidated loop handler targeting the end of every individual client loop tick
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// --- MODULE A: ADMINISTRATION HUD SELECTION CHECKS ---
			while (openHudKey.consumeClick()) {
				if (client.player != null) {
					client.setScreen(new ModHUD(Component.literal("IdHud")));
				}
			}

			// Check if your NoFall checkbox/boolean is enabled from your ModHUD
			if (isElytraBoost && elytraBoostKey.isDown()) {
				isElytraBoosting = true;
			} else {
				isElytraBoosting = false;
			}

			// Perform active physics adjustments if toggled on and player isn't in Spectator Mode
			if (isFlying && client.player != null && !client.player.isSpectator()) {
				// Evaluates raw peripheral inputs directly out of the game engine's core controller maps
				boolean jump = client.options.keyJump.isDown();
				boolean sneak = client.options.keyShift.isDown();

				double speed = 1.5D;
				double ySpeed = jump ? 0.4D : (sneak ? -0.4D : 0.0D);

				// Read structural W/S (Forward) and A/D (Strafe) float multipliers (-1.0 to 1.0)
				net.minecraft.world.phys.Vec2 moveVector = client.player.input.getMoveVector();

				float strafeInput = -moveVector.x;   // Represents left / right strafe weight
				float forwardInput = moveVector.y;  // Represents forward / backward weight

				// Calculate direction angles relative to where the player entity is facing horizontally
				float yaw = client.player.yHeadRot;
				double radYaw = Math.toRadians(yaw);

				// Compute absolute X and Z vector movements based on directional trigonometry matrices
				double motionX = (forwardInput * -Math.sin(radYaw) + strafeInput * -Math.cos(radYaw)) * speed;
				double motionZ = (forwardInput * Math.cos(radYaw) + strafeInput * -Math.sin(radYaw)) * speed;

				// Force the player's underlying mechanical velocity attributes to match direction vectors
				client.player.setDeltaMovement(motionX, ySpeed, motionZ);
			}
		});
	}
}
