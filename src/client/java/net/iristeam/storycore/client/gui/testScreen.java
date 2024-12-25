package net.iristeam.storycore.client.gui;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import static net.iristeam.storycore.client.StoryCoreClient.LOGGER;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


@Environment(EnvType.CLIENT)
public class testScreen extends Screen {
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

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().scale(3.0F, 3.0F, 3.0F);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2 / 3, 30, 13041663);
        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
        if (this.titleScreenButton != null && this.client.getAbuseReportContext().hasDraft()) {
            context.drawTexture(ClickableWidget.WIDGETS_TEXTURE, this.titleScreenButton.getX() + this.titleScreenButton.getWidth() - 17, this.titleScreenButton.getY() + 3, 182, 24, 15, 15);
        }

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

