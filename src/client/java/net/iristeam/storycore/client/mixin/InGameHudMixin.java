package net.iristeam.storycore.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.iristeam.storycore.client.StoryCoreClient.MOD_ID;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public class InGameHudMixin {

    private static final Identifier CLOCKS              = Identifier.of(MOD_ID,"textures/gui/in-game/clocks.png");
    private static final Identifier ANTENA              = Identifier.of(MOD_ID,"textures/gui/in-game/antena1.png");
    private static final Identifier SUN                 = Identifier.of(MOD_ID,"textures/gui/in-game/sun.png");
    private static final Identifier WARNING             = Identifier.of(MOD_ID,"textures/gui/in-game/warning.png");
    private static final Identifier PEREKLADKA1         = Identifier.of(MOD_ID,"textures/gui/in-game/perekladka1.png");
    private static final Identifier PEREKLADKA2         = Identifier.of(MOD_ID,"textures/gui/in-game/perekladka2.png");
    private static final Identifier PEREKLADKA3         = Identifier.of(MOD_ID,"textures/gui/in-game/perekladka3.png");
    private static final Identifier STRELKA_VPRAVO_BLUE = Identifier.of(MOD_ID,"textures/gui/in-game/strelka_vpravo_blue.png");
    private static final Identifier STRELKA_VLEVO_BLUE  = Identifier.of(MOD_ID,"textures/gui/in-game/strelka_vlevo_blue.png");
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    public void render(DrawContext context, float tickDelta, CallbackInfo ci) {
        Window window = MinecraftClient.getInstance().getWindow();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
//        context.drawText(textRenderer, Text.of(String.valueOf(window.getFramebufferWidth())),  10,10,2,false);
        context.drawTexture(ANTENA      , 20, 5, 0, 0, 16, 16,16,16);
        context.drawTexture(PEREKLADKA1 , 4, 25, 0, 0, 96, 1,96,1);
        context.drawTexture(CLOCKS      , window.getScaledWidth()-100, 16, 0, 0, 5, 5, 5, 5);
        context.drawText(textRenderer   , 12 +":00" ,  window.getScaledWidth()-93 , 15,16777215,false);
//        context.drawTexture(CLOCKS      , window.getScaledWidth()-85, 8, 0, 0, 11, 11, 11, 11);
        context.drawTexture(PEREKLADKA3 , window.getScaledWidth()-60, 9, 0, 0, 1, 11, 1, 11);
        context.drawText(textRenderer   , 12 +"°",  window.getScaledWidth()-57 , 15,16777215,false);
        context.drawTexture(PEREKLADKA3 , window.getScaledWidth()-40, 9, 0, 0, 1, 11, 1, 11);
        context.drawTexture(SUN         , window.getScaledWidth()-35, 16, 0, 0, 5, 5, 5, 5);
        context.drawTexture(PEREKLADKA1 , window.getScaledWidth()-100, 25, 0, 0, 96, 1,96,1);
    }
}
