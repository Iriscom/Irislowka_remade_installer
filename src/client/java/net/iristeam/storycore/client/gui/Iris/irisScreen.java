package net.iristeam.storycore.client.gui.Iris;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import java.util.Random;

public class irisScreen extends Screen{
    private static final Identifier STAR_TEXTURE = new Identifier("storycore", "textures/gui/star.png");
    private static final Identifier PANEL_TEXTURE = new Identifier("storycore", "textures/gui/panel.png");

    private static final int BASE_STAR_SIZE = 8;
    private static final int BASE_PADDING = 5;
    private static final int MAP_SIZE = 10000;

    private static final double FRICTION = 0.93;  // Инерция движения
    private static final double MAX_SPEED = 50.0;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 5.0;
    private static final double SCALE_SPEED = 0.15;

    private double offsetX = 0, offsetY = 0;
    private double velocityX = 0, velocityY = 0;
    private boolean dragging = false;

    private double scale = 1.0;
    private double targetScale = 1.0;

    private double focusX = 0, focusY = 0; // Фокус масштабирования

    private final List<int[]> stars = new ArrayList<>();
    private final Random random = new Random();

    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_PADDING = 10;

    public irisScreen() {
        super(Text.of("Starry Sky"));
    }

    @Override
    protected void init() {
        stars.clear();
        for (int x = -MAP_SIZE; x < MAP_SIZE; x += BASE_STAR_SIZE + BASE_PADDING) {
            for (int y = -MAP_SIZE; y < MAP_SIZE; y += BASE_STAR_SIZE + BASE_PADDING) {
                if ((x / (BASE_STAR_SIZE + BASE_PADDING) + y / (BASE_STAR_SIZE + BASE_PADDING)) % 2 == 0) {
                    stars.add(new int[]{x, y});
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableBlend();

        // Обновление позиции с учетом инерции
        if (!dragging) {
            velocityX *= FRICTION;
            velocityY *= FRICTION;
            offsetX += velocityX;
            offsetY += velocityY;
        }

        // Плавное изменение масштаба
        double prevScale = scale;
        scale += (targetScale - scale) * SCALE_SPEED;

        // Коррекция позиции при изменении масштаба
        if (Math.abs(scale - prevScale) > 0.001) {
            offsetX = (offsetX - focusX) * (scale / prevScale) + focusX;
            offsetY = (offsetY - focusY) * (scale / prevScale) + focusY;
        }

        // Чёрный фон
        context.fill(0, 0, width, height, 0xFF000000);

        // Рисуем звёзды
        int starSize = (int) (BASE_STAR_SIZE * scale);

        for (int[] star : stars) {
            int drawX = (int) ((star[0] + offsetX) * scale + width / 2);
            int drawY = (int) ((star[1] + offsetY) * scale + height / 2);

            if (drawX >= -starSize && drawX <= width && drawY >= -starSize && drawY <= height) {
                context.drawTexture(STAR_TEXTURE, drawX, drawY, 0, 0, starSize, starSize, starSize, starSize);
            }
        }

        // Рисуем панель
        renderSidebar(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSidebar(DrawContext context) {
        context.drawTexture(PANEL_TEXTURE, width - PANEL_WIDTH, 0, 0, 0, PANEL_WIDTH, height, 100, 1080);
        context.drawTextWithShadow(textRenderer, Text.of("Right Sidebar"), width - PANEL_WIDTH + PANEL_PADDING, PANEL_PADDING, 0xFFFFFF);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            dragging = true;
            offsetX += deltaX / scale;
            offsetY += deltaY / scale;
            velocityX = deltaX / scale;
            velocityY = deltaY / scale;

            velocityX = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, velocityX));
            velocityY = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, velocityY));

            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double prevTargetScale = targetScale;
        targetScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, targetScale + amount * 0.2));

        // Центрирование масштабирования на точке курсора
        focusX = (mouseX - width / 2) / scale + offsetX;
        focusY = (mouseY - height / 2) / scale + offsetY;

        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

