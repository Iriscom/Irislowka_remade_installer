package net.iristeam.storycore.client.gui;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;


import static com.ibm.icu.text.PluralRules.Operand.j;
import static net.iristeam.storycore.client.StoryCoreClient.LOGGER;
import static net.minecraft.client.gui.screen.LevelLoadingScreen.drawChunkMap;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


@Environment(EnvType.CLIENT)
public class testScreen extends Screen {
    public static final SplashTextRenderer MERRY_X_MAS_ = new SplashTextRenderer("Merry X-mas!");
    private static final int TEXT_X = 123;
    private static final int TEXT_Y = 69;
    private int ticksSinceDeath;
    private final boolean isHardcore;
    private final List<ButtonWidget> buttons = Lists.newArrayList();
    @Nullable
    private ButtonWidget titleScreenButton;

    public testScreen(@Nullable Text message, boolean isHardcore) {
        super(Text.translatable("Hello IrisTEAM!"));
        this.isHardcore = isHardcore;
    }

    protected void init() {
        this.ticksSinceDeath = 0;
        this.buttons.clear();
        Text text = Text.translatable("Tipo cnopka");
        this.buttons.add((ButtonWidget)this.addDrawableChild(ButtonWidget.builder(text, (button) -> {
            button.active = true;
            this.close();
        }).dimensions(this.width / 2 - 100, this.height / 4 + 72, 100, 20).build()));
//        this.buttons.add(this.titleScreenButton);
//        LOGGER.info(this.buttons.toString());
    }


    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().scale(3.0F, 3.0F, 3.0F);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2 / 3, 30, 13041663);
        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().scale(1.5F, 1.5F, 1.5F);
        context.drawItemWithoutEntity(new ItemStack(Blocks.COMMAND_BLOCK),this.width / 4 + 29, this.height / 4 + 18);
        context.setShaderColor(160, 238 , 93, 1);
        context.getMatrices().pop();
        super.render(context, mouseX, mouseY, delta);
    }

    @Nullable



    public boolean shouldPause() {
        return false;
    }


    private void setButtonsActive(boolean active) {
        for(ButtonWidget buttonWidget : this.buttons) {
            buttonWidget.active = active;
        }

    }

    @Environment(EnvType.CLIENT)
    public static class TitleScreenConfirmScreen extends ConfirmScreen {
        public TitleScreenConfirmScreen(BooleanConsumer booleanConsumer, Text text, Text text2, Text text3, Text text4) {
            super(booleanConsumer, text, text2, text3, text4);
        }
    }
}

