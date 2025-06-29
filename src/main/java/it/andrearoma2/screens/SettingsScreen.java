package it.andrearoma2.screens;

import it.andrearoma2.backend.CallbackServer;
import it.andrearoma2.backend.SpotifyLogin;
import it.andrearoma2.backend.SpotifyTokenManager;
import it.andrearoma2.backend.UserInfos;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class SettingsScreen extends Screen {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir();
    private static File tokenFile;
    private static File userInfosFile;
    private static File optionsFile;
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
        GridWidget.Adder adder = gridWidget.createAdder(2);

        // Spotify Login System
        tokenFile = CONFIG_PATH.resolve("minemusic/tokens.properties").toFile();
        userInfosFile = CONFIG_PATH.resolve("minemusic/userInfo.json").toFile();
        optionsFile = CONFIG_PATH.resolve("minemusic/options.properties").toFile();
        Properties optionsProps = loadOptions(optionsFile);

        if (!tokenFile.exists() || !userInfosFile.exists() || !optionsFile.exists()){
            adder.add(this.createStandardButton(Text.translatable("minemusic.loginButtonLabel"), () -> loginButton()));
        } else {
            try {
                if (SpotifyTokenManager.getValidate()){
                    adder.add(this.createStandardButton(Text.literal("Account: " + UserInfos.username(userInfosFile)), () -> logoutButton()));
                } else {
                    SpotifyTokenManager.getAccessToken();
                    adder.add(this.createStandardButton(Text.literal("Account: " + UserInfos.username(userInfosFile)), () -> logoutButton()));
                }

                // Style Button
                String selectedStyle = optionsProps.getProperty("style", "Boss Bar");
                ArrayList<String> styles = new ArrayList<>(List.of(new String[]{"Boss Bar", "Overlay (HUD)", "Chat Message"}));
                adder.add(this.createCyclingButton(styles, optionsFile, selectedStyle, Text.translatable("minemusic.styleButtonLabel"), "style"));
                // Layout Button
                String selectedLayout = optionsProps.getProperty("layout", "Title Only");
                ArrayList<String> layouts = new ArrayList<>(List.of(new String[]{"Title Only", "Title and Artist", "Full Info"}));
                adder.add(this.createCyclingButton(layouts, optionsFile, selectedLayout, Text.translatable("minemusic.layoutButtonLabel"), "layout"));
                // CoverImage Button
                String coverImage = optionsProps.getProperty("coverImage", "OFF");
                ArrayList<String> boolImage = new ArrayList<>(List.of(new String[]{"ON", "OFF"}));
                adder.add(this.createCyclingButton(boolImage, optionsFile, String.valueOf(coverImage), Text.translatable("minemusic.coverImageButtonLabel"),  "coverImage"));

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

    private CyclingButtonWidget<Object> createCyclingButton(ArrayList<String> options, File optionsFile, String selectedValue, Text buttonLabel, String optionKey){
        return CyclingButtonWidget.builder(value -> Text.literal(value.toString()))
                .values(options.toArray())
                .initially(selectedValue)
                .tooltip(value -> {
                    String val = value.toString();
                    Text dynamicTooltip = null;
                    switch (optionKey) {
                        case "style" -> {
                            dynamicTooltip = switch (val) {
                                case "Boss Bar" -> Text.translatable("minemusic.bossbarStyleTooltipText");
                                case "Overlay (HUD)" -> Text.translatable("minemusic.overlayStyleTooltipText");
                                case "Chat Message" -> Text.translatable("minemusic.chatMessageStyleTooltipText");
                                default -> Text.literal("");
                            };
                        }
                        case "layout" -> {
                            dynamicTooltip = switch (val) {
                                case "Title Only" -> Text.translatable("minemusic.titleLayoutTooltipText");
                                case "Title and Artist" -> Text.translatable("minemusic.titleAndArtistLayoutTooltipText");
                                case "Full Info" -> Text.translatable("minemusic.completeLayoutTooltipText");
                                default -> Text.literal("");
                            };
                        }
                        case "coverImage" -> {
                            dynamicTooltip = Text.translatable("minemusic.coverImageButtonTooltip").append(Text.translatable("minemusic.coverImageButtonTooltipWarning").styled(style -> style.withColor(Formatting.RED)));
                        }
                        default -> Text.literal("");
                    }
                    return Tooltip.of(dynamicTooltip);
                })
                .build(
                        this.width / 2 - 100 + 205,
                        this.height / 4 + 48,
                        150,
                        20,
                        buttonLabel,
                        (button, value) -> {
                            String newStyle = value.toString();
                            saveOption(optionsFile, optionKey, newStyle);
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
                        MinecraftClient.getInstance().setScreen(null);
                    } else {
                        MinecraftClient.getInstance().setScreen(new SettingsScreen(this.parent));
                    }
                },
                loginUrl,
                true
        );
    }

    private ConfirmScreen logoutButton(){
        return new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        if (tokenFile.delete() && userInfosFile.delete()) {
                            System.out.println("Logged Out Successfully!");
                        } else {
                            System.out.println("Failed to delete files!");
                        }
                    }
                    MinecraftClient.getInstance().setScreen(new SettingsScreen(this.parent));
                },
                Text.translatable("minemusic.logoutText"),
                Text.translatable("minemusic.logoutConfirmText")
        );
    }

    private Properties loadOptions(File optionsFile) {
        Properties properties = new Properties();
        try (FileReader reader = new FileReader(optionsFile)) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void saveOption(File optionsFile, String key, String value) {
        Properties props = new Properties();
        try {
            if (optionsFile.exists()) {
                try (FileReader reader = new FileReader(optionsFile)) {
                    props.load(reader);
                }
            }
            props.setProperty(key, value);
            try (FileWriter writer = new FileWriter(optionsFile)) {
                props.store(writer, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
