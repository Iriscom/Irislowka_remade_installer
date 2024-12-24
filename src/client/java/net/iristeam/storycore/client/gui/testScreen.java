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

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


@Environment(EnvType.CLIENT)
public class testScreen extends Screen {
    private int ticksSinceDeath;
    private final Text message;
    private final boolean isHardcore;
    private Text scoreText;
    private final List<ButtonWidget> buttons = Lists.newArrayList();
    @Nullable
    private ButtonWidget titleScreenButton;

    public testScreen(@Nullable Text message, boolean isHardcore) {
        super(Text.translatable(isHardcore ? "testScreen.title.hardcore" : "testScreen.title"));
        this.message = message;
        this.isHardcore = isHardcore;
    }

    protected void init() {
        this.ticksSinceDeath = 0;
        this.buttons.clear();
        Text text = this.isHardcore ? Text.translatable("testScreen.spectate") : Text.translatable("testScreen.respawn");
        this.buttons.add((ButtonWidget)this.addDrawableChild(ButtonWidget.builder(text, (button) -> {
            this.client.player.requestRespawn();
            button.active = false;
        }).dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
        this.buttons.add(this.titleScreenButton);
        this.setButtonsActive(false);
        this.scoreText = Text.translatable("testScreen.score").append(": ").append(Text.literal(Integer.toString(this.client.player.getScore())).formatted(Formatting.YELLOW));
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void quitLevel() {
        if (this.client.world != null) {
            this.client.world.disconnect();
        }

        this.client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
        this.client.setScreen(new TitleScreen());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
        context.getMatrices().push();
        context.getMatrices().scale(2.0F, 2.0F, 2.0F);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2 / 2, 30, 16777215);
        context.getMatrices().pop();
        if (this.message != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, this.message, this.width / 2, 85, 16777215);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, this.scoreText, this.width / 2, 100, 16777215);
        if (this.message != null && mouseY > 85) {
            Objects.requireNonNull(this.textRenderer);
            if (mouseY < 85 + 9) {
                Style style = this.getTextComponentUnderMouse(mouseX);
                context.drawHoverEvent(this.textRenderer, style, mouseX, mouseY);
            }
        }

        super.render(context, mouseX, mouseY, delta);
        if (this.titleScreenButton != null && this.client.getAbuseReportContext().hasDraft()) {
            context.drawTexture(ClickableWidget.WIDGETS_TEXTURE, this.titleScreenButton.getX() + this.titleScreenButton.getWidth() - 17, this.titleScreenButton.getY() + 3, 182, 24, 15, 15);
        }

    }

    @Nullable
    private Style getTextComponentUnderMouse(int mouseX) {
        if (this.message == null) {
            return null;
        } else {
            int i = this.client.textRenderer.getWidth(this.message);
            int j = this.width / 2 - i / 2;
            int k = this.width / 2 + i / 2;
            return mouseX >= j && mouseX <= k ? this.client.textRenderer.getTextHandler().getStyleAt(this.message, mouseX - j) : null;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.message != null && mouseY > (double)85.0F) {
            Objects.requireNonNull(this.textRenderer);
            if (mouseY < (double)(85 + 9)) {
                Style style = this.getTextComponentUnderMouse((int)mouseX);
                if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == Action.OPEN_URL) {
                    this.handleTextClick(style);
                    return false;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean shouldPause() {
        return false;
    }

    public void tick() {
        super.tick();
        ++this.ticksSinceDeath;
        if (this.ticksSinceDeath == 20) {
            this.setButtonsActive(true);
        }

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

