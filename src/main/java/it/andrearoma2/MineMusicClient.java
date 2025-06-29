package it.andrearoma2;

import it.andrearoma2.backend.GetSpotifyTrackInfos;
import it.andrearoma2.overlays.MineMusicOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MineMusicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MineMusicOverlay.register();
        long[] lastUpdate = {0};
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (System.currentTimeMillis() - lastUpdate[0] > 1000) {
                GetSpotifyTrackInfos.update();
                lastUpdate[0] = System.currentTimeMillis();
            }

            if (!MineMusicOverlay.id.equals(GetSpotifyTrackInfos.id)) {
                MineMusicOverlay.updateSong(
                        GetSpotifyTrackInfos.songTitle,
                        GetSpotifyTrackInfos.artistName,
                        GetSpotifyTrackInfos.albumTitle,
                        GetSpotifyTrackInfos.durationMs,
                        GetSpotifyTrackInfos.urlImage
                );
                MineMusicOverlay.id = GetSpotifyTrackInfos.id;
                System.out.println("Updated!");
            }

            MineMusicOverlay.updateProgress(GetSpotifyTrackInfos.progressMs);
        });
    }
}

