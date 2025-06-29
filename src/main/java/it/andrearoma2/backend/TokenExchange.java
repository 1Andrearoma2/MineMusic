package it.andrearoma2.backend;

import net.fabricmc.loader.api.FabricLoader;
import net.minidev.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.Properties;

public class TokenExchange {
    public static void exchangeCode(String code){
        try {
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setClientId(SecureConfig.clientId)
                    .setClientSecret(SecureConfig.clientSecret)
                    .setRedirectUri(URI.create(SecureConfig.redirectUri))
                    .build();
            AuthorizationCodeRequest codeRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials creds = codeRequest.execute();
            spotifyApi.setAccessToken(creds.getAccessToken());
            spotifyApi.setRefreshToken(creds.getRefreshToken());

            // Create tokens.properties
            Properties tokens = new Properties();
            tokens.setProperty("access_token", creds.getAccessToken());
            tokens.setProperty("refresh_token", creds.getRefreshToken());
            tokens.setProperty("expires_at", String.valueOf(System.currentTimeMillis() + creds.getExpiresIn() * 1000));
            Path configPath = FabricLoader.getInstance().getConfigDir();
            File tokenFile = configPath.resolve("minemusic/tokens.properties").toFile();
            tokenFile.getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(tokenFile)){
                tokens.store(out, "Spotify Tokens");
            }

            // Create userInfo.json
            User userProfile = spotifyApi.getCurrentUsersProfile().build().execute();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("displayName", userProfile.getDisplayName());
            jsonObject.put("accountUrl", URI.create(userProfile.getExternalUrls().getExternalUrls().get("spotify")).toString());
            jsonObject.put("accountImage", userProfile.getImages()[1]);
            FileWriter userInfoFile = new FileWriter(configPath.resolve("minemusic/userInfo.json").toFile());
            userInfoFile.write(jsonObject.toJSONString());
            userInfoFile.close();

            // Create options.properties
            if (!configPath.resolve("minemusic/options.properties").toFile().exists()){
                Properties options = new Properties();
                options.setProperty("style", "Boss Bar"); // ["Boss Bar", "Overlay (HUD)", "Chat Message"]
                options.setProperty("layout", "Title Only"); // ["Title Only", "Title and Artist", "Full Info"]
                options.setProperty("coverImage", "OFF"); // ONLY OVERLAY ["ON", "OFF"]
                File optionsFile = configPath.resolve("minemusic/options.properties").toFile();
                try (FileOutputStream out = new FileOutputStream(optionsFile)){
                    options.store(out, null);
                }
            }

            System.out.println("Loggato!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
