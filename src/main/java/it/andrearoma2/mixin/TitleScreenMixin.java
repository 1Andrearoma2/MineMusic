package it.andrearoma2.mixin;

import it.andrearoma2.screens.SettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title){
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void addsModsButton(CallbackInfo ci){
        this.addDrawableChild(TextIconButtonWidget.builder(
                Text.empty(),
                (button) -> {
                    MinecraftClient.getInstance().setScreen(new SettingsScreen(this));
                }, true)
                .texture(Identifier.of("minemusic:icon/logo"), 17, 17)
                .width(20)
                .build()
        ).setPosition(this.width / 2 - 100 + 205, this.height / 4 + 48);
    }
}
