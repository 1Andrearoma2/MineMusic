package it.andrearoma2.backend;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.AuthorizationScope;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.net.URI;

public class SpotifyLogin {
    public static String generateLoginLink(){
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(SecureConfig.clientId)
                .setRedirectUri(URI.create(SecureConfig.redirectUri))
                .build();

        AuthorizationCodeUriRequest uriRequest = spotifyApi.authorizationCodeUri()
                .scope(AuthorizationScope.USER_MODIFY_PLAYBACK_STATE, AuthorizationScope.USER_READ_PLAYBACK_STATE, AuthorizationScope.USER_READ_PRIVATE, AuthorizationScope.APP_REMOTE_CONTROL, AuthorizationScope.PLAYLIST_READ_PRIVATE)
                .show_dialog(false)
                .build();

        URI authUri = uriRequest.execute();
        return authUri.toString();
    }
}
