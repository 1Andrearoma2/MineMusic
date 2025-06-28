package it.andrearoma2.screens;

import it.andrearoma2.backend.CallbackServer;
import it.andrearoma2.backend.SpotifyLogin;
import it.andrearoma2.backend.SpotifyTokenManager;
import it.andrearoma2.backend.UserInfos;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SettingsScreen extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("minemusic.settingsScreenTitle");
    protected final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 61, 33);
    public SettingsScreen(Screen parent){
        super(TITLE_TEXT);
        this.parent = parent;
    }

    protected void init(){
        // Screen Layout
        DirectionalLayoutWidget directionalLayoutWidget = (DirectionalLayoutWidget) this.layout.addHeader(DirectionalLayoutWidget.vertical().spacing(0));
        directionalLayoutWidget.add(new TextWidget(TITLE_TEXT, this.textRenderer), Positioner::alignHorizontalCenter);
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(1);

        // Spotify Login System
        Path configPath = FabricLoader.getInstance().getConfigDir();
        File tokenFile = configPath.resolve("minemusic/tokens.properties").toFile();
        File userInfosFile = configPath.resolve("minemusic/userInfo.json").toFile();
        File optionsFile = configPath.resolve("minemusic/options.properties").toFile();

        if (!tokenFile.exists() || !userInfosFile.exists() || !optionsFile.exists()){
            adder.add(this.createStandardButton(Text.translatable("minemusic.loginButtonLabel"), () -> loginButton()));
        } else {
            try {
                if (SpotifyTokenManager.getValidate()){
                    adder.add(this.createStandardButton(Text.literal("Account: " + UserInfos.username(userInfosFile)), () -> loginButton()));
                    adder.add(this.createStandardButton(Text.literal("Account: " + UserInfos.username(userInfosFile)), () -> loginButton()));
                } else {
                    SpotifyTokenManager.getAccessToken();
                    adder.add(this.createStandardButton(Text.literal("Account: " + UserInfos.username(userInfosFile)), () -> loginButton()));
                }

                // Style Button
                ArrayList<String> options = new ArrayList<>(List.of(new String[]{"negro", "frocio", "ricchione"}));
                adder.add(this.createCyclingButton(options, null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.layout.addBody(gridWidget);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).width(200).build());
        this.layout.forEachChild((child) -> {
            ClickableWidget var1000 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.layout.refreshPositions();
    }

    private ButtonWidget createStandardButton(Text message, Supplier<Screen> screenSupplier){
        return ButtonWidget.builder(message, (button) -> {
            this.client.setScreen((Screen)screenSupplier.get());
        }).build();
    }

    private CyclingButtonWidget<Object> createCyclingButton(ArrayList<String> options, Supplier<Screen> onClick){
        return CyclingButtonWidget.builder(value -> Text.literal(value.toString()))
                .values(options.toArray())
                .initially(options.getFirst())
                .build(
                        this.width / 2 - 100 + 205,
                        this.height / 4 + 48,
                        120,
                        20,
                        Text.translatable("minemusic.styleButtonLabel"),
                        (button, value) -> {
                            System.out.println("Selezionato: " + value);
                            //MinecraftClient.getInstance().setScreen(onClick.get());
                        }
                );
    }

    private ConfirmLinkScreen loginButton(){
        new Thread(() -> {
            try {
                CallbackServer.start();
            } catch (Exception e){
                e.printStackTrace();
            }
        }).start();
        String loginUrl = SpotifyLogin.generateLoginLink();

        return new ConfirmLinkScreen(
                confirmed -> {
                    if (confirmed){
                        Util.getOperatingSystem().open(loginUrl);
                    }
                    MinecraftClient.getInstance().setScreen(this);
                },
                loginUrl,
                true
        );
    }
}
