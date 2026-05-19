package com.tcc.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class TrafficStopClient implements ClientModInitializer {

	// 1. Persistent fields for HUD and configuration settings
	private static KeyMapping openHudKey;
	public static boolean isWebSpoofActive = false;

	// 2. Persistent fields for the velocity movement modules
	private static KeyMapping flyKey;
	public static boolean isFlying = false;

	@Override
	public void onInitializeClient() {
		// 3. Register the HUD initialization key binding mapping configuration (Default: 'M' key)
		openHudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"Traffic Stop Client - Menu Open",          // Localized translation key for the Options menu
				InputConstants.Type.KEYSYM,          // Specifies that this maps to a peripheral keyboard sensor
				GLFW.GLFW_KEY_M,                     // Default bound key on launch
				KeyMapping.Category.MISC             // Categorizes it in the game's menu layout under 'Miscellaneous'
		));

		// 5. Register the single consolidated loop handler targeting the end of every individual client loop tick
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// --- MODULE A: ADMINISTRATION HUD SELECTION CHECKS ---
			while (openHudKey.consumeClick()) {
				if (client.player != null) {
					client.setScreen(new ModHUD(Component.literal("Administration HUD")));
				}
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
