package it.andrearoma2.overlays;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MineMusicOverlay {
    private static String title;
    private static String artist;
    private static String album;
    private static int durationMs;
    private static int progressMs;
    public static String id = "0";

    private static Identifier coverTexture = Identifier.of("minemusic", "default_cover");
    private static final Identifier OVERLAY_LAYER_ID = Identifier.of("minemusic", "spotify_overlay");

    public static void register(){
        HudLayerRegistrationCallback.EVENT.register((registrar) -> {
            registrar.attachLayerBefore(IdentifiedLayer.CHAT, OVERLAY_LAYER_ID, MineMusicOverlay::renderOverlay);
        });
    }

    public static void updateSong(String newTitle, List<String> newArtist, String newAlbum, int newDuration, String imageUrl) {
        title = newTitle;
        artist = newArtist.toString().replace("[", "").replace("]", "");
        album = newAlbum;
        durationMs = newDuration;
        progressMs = 0;
        loadCoverFromUrl(imageUrl);
    }

    public static void updateProgress(int newProgress) {
        progressMs = newProgress;
    }

    private static void renderOverlay(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        TextRenderer textRenderer = client.textRenderer;
        int x = 10;
        int y = 10;

        // Cover (64x64)
        drawContext.drawTexture(RenderLayer::getGuiTextured, coverTexture, x, y, 0, 0, 64, 64, 64, 64);

        int textX = x + 70;
        int textY = y;

        drawContext.drawText(textRenderer, title + " - " + artist, textX, textY, 0xFFFFFF, false);
        drawContext.drawText(textRenderer, album, textX + 30, textY + 12, 0xAAAAAA, false);

        // Progress bar
        float ratio = (float) progressMs / durationMs;
        int barWidth = 100;
        int barHeight = 5;
        int barX = textX;
        int barY = textY + 30;

        // Sfondo
        drawContext.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF444444);
        // Avanzamento
        drawContext.fill(barX, barY, barX + (int) (barWidth * ratio), barY + barHeight, 0xFF1DB954);

        // Testo tempi
        String leftTime = formatTime(progressMs);
        String rightTime = formatTime(durationMs);
        drawContext.drawText(textRenderer, leftTime, barX, barY + 6, 0xAAAAAA, false);
        drawContext.drawText(textRenderer, rightTime, barX + barWidth - textRenderer.getWidth(rightTime), barY + 6, 0xAAAAAA, false);
    }

    private static String formatTime(int ms) {
        int totalSec = ms / 1000;
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }

    private static void loadCoverFromUrl(String urlString) {
        CompletableFuture.runAsync(() -> {
            try (InputStream stream = new URL(urlString).openStream()) {
                BufferedImage bufferedImage = ImageIO.read(stream);
                if (bufferedImage == null) {
                    System.err.println("Errore: Immagine non valida o non supportata!");
                    return;
                }
                NativeImage nativeImage = convertBufferedImageToNativeImage(bufferedImage);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
                Identifier id = Identifier.of("minemusic", "cover");
                MinecraftClient.getInstance().execute(() -> {
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
                    coverTexture = id;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Util.getMainWorkerExecutor());
    }

    private static NativeImage convertBufferedImageToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, false); // false = non alpha premultiplied

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bufferedImage.getRGB(x, y);

                // ARGB (Java) -> RGBA (NativeImage)
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int rgba = (r << 24) | (g << 16) | (b << 8) | a;
                nativeImage.setColorArgb(x, y, rgba);
            }
        }
        return nativeImage;
    }
}
