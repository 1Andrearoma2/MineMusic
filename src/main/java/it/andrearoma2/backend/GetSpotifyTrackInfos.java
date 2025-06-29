package it.andrearoma2.backend;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GetSpotifyTrackInfos {

    public static String songTitle;
    public static List<String> artistName = new ArrayList<>();
    public static String albumTitle;
    public static int durationMs;
    public static int progressMs;
    public static String id;
    public static String urlImage;

    public static void update() {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(SecureConfig.clientId)
                .setClientSecret(SecureConfig.clientSecret)
                .setRedirectUri(URI.create(SecureConfig.redirectUri))
                .build();
        loadTokens(spotifyApi);
        try {
            CurrentlyPlaying currentlyPlaying = spotifyApi.getUsersCurrentlyPlayingTrack().build().execute();
            if (currentlyPlaying == null || currentlyPlaying.getItem() == null) return;

            Track track = (Track) currentlyPlaying.getItem();
            progressMs = currentlyPlaying.getProgress_ms();
            durationMs = track.getDurationMs();
            songTitle = track.getName();

            artistName.clear();
            for (ArtistSimplified artist : track.getArtists()) {
                artistName.add(artist.getName());
            }

            albumTitle = track.getAlbum().getName();
            id = track.getId();
            urlImage = track.getAlbum().getImages()[1].getUrl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void loadTokens(SpotifyApi spotifyApi){
        Properties tokens = new Properties();
        try {
            if (!SpotifyTokenManager.getValidate()){
                SpotifyTokenManager.getAccessToken();
            }
            try (FileReader reader = new FileReader(FabricLoader.getInstance().getConfigDir().resolve("minemusic/tokens.properties").toFile())) {
                tokens.load(reader);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        spotifyApi.setAccessToken(tokens.getProperty("access_token"));
        spotifyApi.setRefreshToken(tokens.getProperty("refresh_token"));
    }
}
