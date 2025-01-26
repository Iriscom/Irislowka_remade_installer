package net.iristeam.storycore.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import static net.iristeam.storycore.client.StoryCoreClient.MOD_ID;


@Environment(EnvType.CLIENT)
public class testrScreen extends Screen {
    private final List<List<Point>> allPoints = new ArrayList<List<Point>>();  // Список для хранения всех линий (каждая линия - список точек)
    private static final Identifier LINE_TEXTURE = new Identifier(MOD_ID, "textures/gui/line.png");  // Текстура для рисования линии
    private List<Point> currentLine = null;  // Список точек для текущей линии
    private Vector2i lastMousePos = null;  // Переменная для последней позиции мыши
    private static final int LINE_SMOOTHNESS = 5;  // Количество промежуточных точек для плавности линии
    public testrScreen() {
        super(Text.literal("Test Screen"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Проверка, зажата ли левая кнопка мыши
        boolean isPressed = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;

        // Если кнопка нажата и мы не рисуем сейчас, начинаем рисование новой линии
        if (isPressed && currentLine == null) {
            currentLine = new ArrayList<Point>();  // Инициализация новой линии
            currentLine.add(new Point(mouseX, mouseY));  // Добавляем начальную точку
        }

        // Если кнопка зажата, добавляем новую точку, если мышь перемещается
        if (isPressed && (lastMousePos == null || mouseX != lastMousePos.x || mouseY != lastMousePos.y)) {
            currentLine.add(new Point(mouseX, mouseY));  // Добавляем точку в текущую линию
        }

        // Когда кнопка отпускается, сохраняем текущую линию в общий список
        if (!isPressed && currentLine != null) {
            allPoints.add(currentLine);  // Сохраняем текущую линию в список всех линий
            currentLine = null;  // Очищаем текущую линию для начала новой
        }

        // Обновляем последнюю позицию мыши
        if (isPressed) {
            lastMousePos = new Vector2i(mouseX, mouseY);
        }

        // Отображаем все линии
        renderLines(context);
    }

    // Линейная интерполяция между двумя точками
    private List<Point> interpolate(Point start, Point end, int smoothness) {
        List<Point> interpolatedPoints = new ArrayList<>();
        double deltaX = end.x - start.x;  // Обращаемся к полям x и y
        double deltaY = end.y - start.y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        for (int i = 0; i <= smoothness; i++) {
            double t = i / (double) smoothness;  // Пропорция от 0 до 1
            int x = (int) (start.x + t * deltaX);  // Используем x и y напрямую
            int y = (int) (start.y + t * deltaY);
            interpolatedPoints.add(new Point(x, y));
        }

        return interpolatedPoints;
    }

    // Отрисовываем все линии, сохраненные в списке
    private void renderLines(DrawContext context) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);  // Белый цвет

        // Получаем буфер для отрисовки
        MatrixStack matrices = context.getMatrices();

        // Отображаем все линии
        for (List<Point> line : allPoints) {
            if (line.size() < 2) continue;  // Нужно хотя бы 2 точки, чтобы нарисовать линию

            // Интеграция каждой пары точек в линии для плавного рисования
            for (int i = 1; i < line.size(); i++) {
                Point start = line.get(i - 1);
                Point end = line.get(i);

                // Получаем промежуточные точки между точками start и end
                List<Point> smoothPoints = interpolate(start, end, LINE_SMOOTHNESS);

                // Рисуем каждую промежуточную точку
                for (int j = 1; j < smoothPoints.size(); j++) {
                    Point current = smoothPoints.get(j - 1);
                    Point next = smoothPoints.get(j);

                    // Получаем угол между точками
                    double angle = Math.atan2(next.y - current.y, next.x - current.x);  // Используем x и y напрямую
                    double distance = Math.sqrt(Math.pow(next.x - current.x, 2) + Math.pow(next.y - current.y, 2));

                    // Рисуем линию между точками
                    matrices.push();

                    // Перемещаем начало линии в точку начала
                    matrices.translate(current.x, current.y, 0);  // Используем x и y напрямую

                    // Вращаем на угол между точками
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.toDegrees(angle)));

                    // Рисуем текстуру линии
                    context.drawTexture(LINE_TEXTURE, 0, 0, 0, 0, (int) distance, 2);  // Отображение линии

                    matrices.pop();
                }
            }
        }

        // Если рисуем текущую линию, то она отображается на экране
        if (currentLine != null && currentLine.size() > 1) {
            Point start = currentLine.get(currentLine.size() - 2);
            Point end = currentLine.get(currentLine.size() - 1);
            List<Point> smoothPoints = interpolate(start, end, LINE_SMOOTHNESS);

            for (int i = 1; i < smoothPoints.size(); i++) {
                Point current = smoothPoints.get(i - 1);
                Point next = smoothPoints.get(i);

                // Получаем угол между точками
                double angle = Math.atan2(next.y - current.y, next.x - current.x);  // Используем x и y напрямую
                double distance = Math.sqrt(Math.pow(next.x - current.x, 2) + Math.pow(next.y - current.y, 2));

                // Рисуем линию между точками
                matrices.push();

                // Перемещаем начало линии в точку начала
                matrices.translate(current.x, current.y, 0);  // Используем x и y напрямую

                // Вращаем на угол между точками
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.toDegrees(angle)));

                // Рисуем текстуру линии
                context.drawTexture(LINE_TEXTURE, 0, 0, 0, 0, (int) distance, 2);  // Отображение линии

                matrices.pop();
            }
        }
    }

    // Класс для представления точки
    public static class Point {
        public final int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}