package it.andrearoma2.backend;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Properties;
import java.util.stream.Collectors;

public class SpotifyTokenManager {
    private static final Path configPath = FabricLoader.getInstance().getConfigDir();
    private static final String TOKEN_FILE = configPath.resolve("minemusic/tokens.properties").toString();

    public static boolean getValidate() throws IOException {
        Properties tokens = new Properties();
        try (FileInputStream in = new FileInputStream(TOKEN_FILE)){
            tokens.load(in);
        }
        long expiresAt = Long.parseLong(tokens.getProperty("expires_at"));
        if (System.currentTimeMillis() >= expiresAt) return false;
        return true;
    }

    public static String getAccessToken() throws IOException {
        Properties tokens = new Properties();
        try (FileInputStream in = new FileInputStream(TOKEN_FILE)){
            tokens.load(in);
        }

        long expiresAt = Long.parseLong(tokens.getProperty("expires_at"));
        if (System.currentTimeMillis() >= expiresAt){
            return refreshAccessToken(tokens.getProperty("refresh_token"));
        }

        return tokens.getProperty("access_token");
    }

    private static String refreshAccessToken(String refreshToken) throws IOException {
        URL url = new URL("https://accounts.spotify.com/api/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        String data = "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8");

        String auth = Base64.getEncoder().encodeToString((SecureConfig.clientId + ":" + SecureConfig.clientSecret).getBytes());
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()){
            os.write(data.getBytes());
        }

        String response = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                .lines().collect(Collectors.joining());

        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("error")){
            String error = json.get("error").getAsString();
            String errorDesc = json.has("error_description") ? json.get("error_description").getAsString(): "";
            throw new IOException("Errore nel refresh token: " + error + "-" + errorDesc);
        }

        String accessToken = json.get("access_token").getAsString();
        long expiresIn = json.get("expires_in").getAsLong();

        Properties tokens = new Properties();
        tokens.setProperty("access_token", accessToken);
        tokens.setProperty("refresh_token", refreshToken);
        tokens.setProperty("expires_at", String.valueOf(System.currentTimeMillis() + expiresIn * 1000));
        try (FileOutputStream out = new FileOutputStream(TOKEN_FILE)){
            tokens.store(out, "Spotify Tokens");
        }

        return accessToken;
    }
}
