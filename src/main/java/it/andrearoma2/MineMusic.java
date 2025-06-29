package it.andrearoma2;

import it.andrearoma2.backend.GetSpotifyTrackInfos;
import it.andrearoma2.overlays.MineMusicOverlay;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MineMusic implements ModInitializer {

	private static final Identifier EXAMPLE_LAYER = Identifier.of("minemusic", "hud-example-layer");

	@Override
	public void onInitialize() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.execute(() -> {
				MinecraftClient.getInstance().player.sendMessage(Text.literal("🎶 MineMusic è attivo!"), true);
			});
		});
	}
}