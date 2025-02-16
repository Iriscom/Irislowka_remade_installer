package net.iristeam.storycore.client.gui.Iris;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class irisScreen extends Screen {

    // Текстуры
    private static final Identifier STAR_TEXTURE = new Identifier("storycore", "textures/gui/star.png");
    private static final Identifier PANEL_TEXTURE = new Identifier("storycore", "textures/gui/left_menu_iris.png");
    private static final Identifier DISK_TEXTURE = new Identifier("storycore", "textures/gui/disk.png");
    private static final Identifier FOLDER_TEXTURE = new Identifier("storycore", "textures/gui/folder.png");
    // Текстуры для кнопок оверлея
    private static final Identifier OPEN_TEXTURE = new Identifier("storycore", "textures/gui/open.png");
    private static final Identifier DELETE_TEXTURE = new Identifier("storycore", "textures/gui/delete.png");
    // Текстура для частицы, движущейся по линии соединения
    private static final Identifier PARTICLE_TEXTURE = new Identifier("storycore", "textures/gui/particle.png");

    // Параметры звёзд
    private static final int BASE_STAR_SIZE = 8;
    private static final int BASE_PADDING = 5;
    private static final int MAP_SIZE = 10000;

    // Инерция и масштаб для фоновой области
    private static final double FRICTION = 0.93;
    private static final double MAX_SPEED = 50.0;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 5.0;
    private static final double SCALE_SPEED = 0.15;

    // Положение "камеры" (фон)
    private double offsetX = 0, offsetY = 0;
    private double velocityX = 0, velocityY = 0;
    private boolean dragging = false;

    // Масштаб (зум) фона
    private double scale = 1.0;
    private double targetScale = 1.0;
    private double focusX = 0, focusY = 0;

    // Звёзды
    private final List<int[]> stars = new ArrayList<>();
    private final Random random = new Random();

    // Параметры правой панели
    private static final int RIGHT_PANEL_WIDTH = 120;
    private static final int PANEL_PADDING = 10;
    private static final int ITEM_HEIGHT = 20;
    private static final int SCROLL_SPEED = 1;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int MIN_SCROLLBAR_HEIGHT = 30;
    private static final double SCROLL_SMOOTHNESS = 1;

    // Изменён список элементов
    private final List<String> items = List.of(
            "Общее", "Углубленные", "Wiki", "Элемент 4", "Элемент 5",
            "Элемент 6", "Элемент 7", "Элемент 8", "Элемент 9", "Элемент 10",
            "Элемент 11", "Элемент 12", "Элемент 13", "Элемент 14", "Элемент 15",
            "Элемент 16", "Элемент 17"
    );
    private int scrollOffset = 0;
    private double smoothScrollOffset = 0;
    private int maxScrollOffset = 0;

    // Флаг детальной панели и выбранный элемент
    private boolean detailActive = false;
    private int selectedItemIndex = -1;

    // Блоки, созданные на фоне (и триггеры)
    private final List<CustomBlock> backgroundBlocks = new ArrayList<>();

    // Соединения между блоками/триггерами
    private final List<Connection> connections = new ArrayList<>();
    // При зажатом Shift+ПКМ сохраняется выбранный начальный блок для соединения
    private CustomBlock connectionStartBlock = null;

    // Контекстное меню для фоновой области (создание блоков/триггеров, удаление)
    private boolean backgroundContextMenuActive = false;
    private int backgroundContextMenuX = 0, backgroundContextMenuY = 0;
    private static final int CONTEXT_MENU_WIDTH = 150;
    private static final int CONTEXT_OPTION_HEIGHT = 20;
    // Ссылка на блок (или триггер), по которому был совершен ПКМ (если таковой есть)
    private CustomBlock contextBlock = null;

    // Пути для конфигов – создаётся папка "config/Storycore"
    private static final String CONFIG_FOLDER = "config/Storycore";

    // Параметры для модального окна (оверлея) конфигов
    private boolean configOverlayActive = false;
    private List<File> configFiles = new ArrayList<>();
    private final int configOverlayWidth = 300;
    private final int configOverlayHeight = 200;
    // Для выделения выбранного файла в оверлее
    private int selectedConfigIndex = -1;

    // Поля для оверлея настройки триггера (для триггерных блоков)
    private boolean inputOverlayActive = false;
    private CustomBlock activeInputBlock = null;
    private long lastInputClickTime = 0;
    private CustomBlock lastInputClickedBlock = null;

    // Поля для оверлея настройки действий (для обычных блоков)
    private boolean actionOverlayActive = false;
    private CustomBlock activeActionBlock = null;
    private long lastActionClickTime = 0;
    private CustomBlock lastActionClickedBlock = null;

    // Константа – высота области, где отображается поле ввода в блоке
    private static final int INPUT_AREA_HEIGHT = 20;
    // Используем одни размеры для обоих оверлеев (action и input)
    private final int inputOverlayWidth = 300;
    private final int inputOverlayHeight = 150;

    // Новые поля для плавного скролла в проводнике (конфиг-оверлей)
    private int configScrollOffset = 0;
    private double configSmoothScrollOffset = 0;

    public irisScreen() {
        super(Text.of("Starry Sky"));
    }

    @Override
    protected void init() {
        int visibleHeight = this.height - PANEL_PADDING * 2 - 40;
        int maxVisibleItems = visibleHeight / ITEM_HEIGHT;
        maxScrollOffset = Math.max(0, items.size() - maxVisibleItems);

        stars.clear();
        for (int x = -MAP_SIZE; x < MAP_SIZE; x += BASE_STAR_SIZE + BASE_PADDING) {
            for (int y = -MAP_SIZE; y < MAP_SIZE; y += BASE_STAR_SIZE + BASE_PADDING) {
                if ((x / (BASE_STAR_SIZE + BASE_PADDING) + y / (BASE_STAR_SIZE + BASE_PADDING)) % 2 == 0) {
                    stars.add(new int[]{x, y});
                }
            }
        }

        // Попытка загрузки сцены из файла по умолчанию (например, scene.json)
        File defaultFile = new File(CONFIG_FOLDER, "scene.json");
        if (defaultFile.exists()) {
            loadScene(defaultFile);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableBlend();

        // Если оверлей конфигов открыт, отрисовываем только его
        if (configOverlayActive) {
            context.fill(0, 0, width, height, 0xFF2A2A2A);
            renderConfigOverlay(context, mouseX, mouseY);
            return;
        }
        // Если открыт оверлей настройки триггера – отрисовываем его
        if (inputOverlayActive) {
            context.fill(0, 0, width, height, 0xFF2A2A2A);
            renderInputOverlay(context, mouseX, mouseY);
            return;
        }
        // Если открыт оверлей настройки действий – отрисовываем его
        if (actionOverlayActive) {
            context.fill(0, 0, width, height, 0xFF2A2A2A);
            renderActionOverlay(context, mouseY); // передаём mouseY для обработки (если нужно)
            return;
        }

        // Обычная отрисовка основного GUI
        smoothScrollOffset += (scrollOffset - smoothScrollOffset) * SCROLL_SMOOTHNESS;
        if (!dragging) {
            velocityX *= FRICTION;
            velocityY *= FRICTION;
            offsetX += velocityX;
            offsetY += velocityY;
        }
        double oldScale = scale;
        scale += (targetScale - scale) * SCALE_SPEED;
        if (Math.abs(scale - oldScale) > 1e-3) {
            offsetX = (offsetX - focusX) * (scale / oldScale) + focusX;
            offsetY = (offsetY - focusY) * (scale / oldScale) + focusY;
        }
        context.fill(0, 0, width, height, 0xFF000000);
        renderStars(context);
        // Отрисовка в области фона (с scissor для исключения правой панели)
        enableScissor(0, 0, width - RIGHT_PANEL_WIDTH, height);
        // Сначала отрисовываем линии соединений
        renderConnectionLines(context);
        // Затем сами блоки/триггеры
        renderBackgroundBlocks(context);
        // Затем частицы, движущиеся по соединениям
        renderConnectionParticles(context, delta);
        disableScissor();
        renderRightPanel(context, mouseX, mouseY);
        if (backgroundContextMenuActive) {
            renderBackgroundContextMenu(context);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Рисует звёздный фон.
     */
    private void renderStars(DrawContext context) {
        int starSize = (int) (BASE_STAR_SIZE * scale);
        for (int[] star : stars) {
            int drawX = (int) ((star[0] + offsetX) * scale + width / 2);
            int drawY = (int) ((star[1] + offsetY) * scale + height / 2);
            if (drawX >= -starSize && drawX <= width && drawY >= -starSize && drawY <= height) {
                context.drawTexture(STAR_TEXTURE, drawX, drawY, 0, 0, starSize, starSize, starSize, starSize);
            }
        }
    }

    /**
     * Рисует блоки (и триггеры), созданные на фоне.
     * Для триггеров отображается текст, установленный через оверлей настройки клавиши (Press: ...),
     * для обычных блоков – текст с настройками действий.
     */
    private void renderBackgroundBlocks(DrawContext context) {
        for (CustomBlock block : backgroundBlocks) {
            int screenX = (int) ((block.x + offsetX) * scale + width / 2);
            int screenY = (int) ((block.y + offsetY) * scale + height / 2);
            int blockScreenWidth = (int) (block.width * scale);
            int blockScreenHeight = (int) (block.height * scale);
            int fillColor = block.isTrigger ? 0xFF9DDAD0 : 0xFF444444;
            context.fill(screenX, screenY, screenX + blockScreenWidth, screenY + blockScreenHeight, fillColor);
            context.drawTextWithShadow(textRenderer, Text.of(block.blockText), screenX + 4, screenY + 4, 0xFFFFFF);
            int tfScreenHeight = (int) (14 * scale);
            int tfScreenY = screenY + blockScreenHeight - tfScreenHeight - 4;
            context.fill(screenX + 4, tfScreenY, screenX + blockScreenWidth - 4, tfScreenY + tfScreenHeight, 0xFF666666);
            String displayText;
            if (block.isTrigger) {
                displayText = block.inputText.isEmpty() ? "input" : block.inputText;
            } else {
                if (block.actionType != 0) {
                    if (block.actionType == 1) {
                        displayText = "Action: Шок";
                    } else if (block.actionType == 2) {
                        displayText = "Action: Барьер";
                    } else {
                        displayText = "input";
                    }
                } else {
                    displayText = "input";
                }
            }
            context.drawTextWithShadow(textRenderer, Text.of(displayText), screenX + 6, tfScreenY + 2, 0xFFFFFF);
        }
    }

    /**
     * Рисует линии соединений между блоками/триггерами.
     */
    private void renderConnectionLines(DrawContext context) {
        for (Connection conn : connections) {
            int startX = (int) ((conn.start.x + offsetX) * scale + width / 2 + (conn.start.width * scale) / 2);
            int startY = (int) ((conn.start.y + offsetY) * scale + height / 2 + (conn.start.height * scale) / 2);
            int endX = (int) ((conn.end.x + offsetX) * scale + width / 2 + (conn.end.width * scale) / 2);
            int endY = (int) ((conn.end.y + offsetY) * scale + height / 2 + (conn.end.height * scale) / 2);
            int dx = endX - startX;
            int dy = endY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            int step = 4;
            for (int i = 0; i < distance; i += step) {
                int x = startX + (int) (dx * (i / distance));
                int y = startY + (int) (dy * (i / distance));
                context.fill(x, y, x + 2, y + 2, 0xFF888888);
            }
        }
    }

    /**
     * Обновляет и отрисовывает движущиеся частицы вдоль соединений.
     */
    private void renderConnectionParticles(DrawContext context, float delta) {
        for (Connection conn : connections) {
            int startX = (int) ((conn.start.x + offsetX) * scale + width / 2 + (conn.start.width * scale) / 2);
            int startY = (int) ((conn.start.y + offsetY) * scale + height / 2 + (conn.start.height * scale) / 2);
            int endX = (int) ((conn.end.x + offsetX) * scale + width / 2 + (conn.end.width * scale) / 2);
            int endY = (int) ((conn.end.y + offsetY) * scale + height / 2 + (conn.end.height * scale) / 2);

            conn.particleProgress += delta * conn.speed;
            if (conn.particleProgress > 1f) {
                conn.particleProgress -= 1f;
            }
            float targetX = startX + ((endX - startX) * conn.particleProgress);
            float targetY = startY + ((endY - startY) * conn.particleProgress);

            float smoothing = 0.2f;
            if (!conn.smoothingInitialized) {
                conn.smoothParticleX = targetX;
                conn.smoothParticleY = targetY;
                conn.smoothingInitialized = true;
            } else {
                conn.smoothParticleX += (targetX - conn.smoothParticleX) * smoothing;
                conn.smoothParticleY += (targetY - conn.smoothParticleY) * smoothing;
            }

            int particleSize = 8;
            context.drawTexture(PARTICLE_TEXTURE,
                    Math.round(conn.smoothParticleX) - particleSize / 2,
                    Math.round(conn.smoothParticleY) - particleSize / 2,
                    0, 0, particleSize, particleSize, 16, 16);
        }
    }

    /**
     * Рисует правую панель с заголовком и кнопками (дискета и проводник).
     */
    private void renderRightPanel(DrawContext context, int mouseX, int mouseY) {
        int panelX = width - RIGHT_PANEL_WIDTH;
        int panelY = 0;
        int panelHeight = height;
        context.drawTexture(PANEL_TEXTURE, panelX, panelY, 0, 0, RIGHT_PANEL_WIDTH, panelHeight, 100, 1080);
        context.drawTextWithShadow(textRenderer, Text.of("Настройки"), panelX + PANEL_PADDING, panelY + PANEL_PADDING, 0xFFFFFF);

        int diskSize = 16;
        int diskX = panelX + RIGHT_PANEL_WIDTH - PANEL_PADDING - diskSize;
        int diskY = PANEL_PADDING;
        context.drawTexture(DISK_TEXTURE, diskX, diskY, 0, 0, diskSize, diskSize, diskSize, diskSize);

        int folderSize = 16;
        int folderX = diskX - PANEL_PADDING - folderSize;
        int folderY = PANEL_PADDING;
        context.drawTexture(FOLDER_TEXTURE, folderX, folderY, 0, 0, folderSize, folderSize, folderSize, folderSize);

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        float textScale = 0.5f;
        matrixStack.scale(textScale, textScale, 1.0f);
        int scaledX = (int) ((panelX + PANEL_PADDING) / textScale);
        int scaledY = (int) ((panelY + PANEL_PADDING + 12) / textScale);
        context.drawTextWithShadow(textRenderer, Text.of("скрипта"), scaledX, scaledY, 0xFFFFFF);
        matrixStack.pop();

        int contentX = panelX + PANEL_PADDING;
        int contentY = PANEL_PADDING + 40;
        int contentWidth = RIGHT_PANEL_WIDTH - PANEL_PADDING * 2;
        int contentHeight = panelHeight - (contentY + PANEL_PADDING);

        if (detailActive) {
            renderDetailPanel(context, panelX, panelY, RIGHT_PANEL_WIDTH, panelHeight);
        } else {
            enableScissor(contentX, contentY, contentWidth, contentHeight);
            renderItemList(context, contentX, contentY, contentWidth, contentHeight, mouseX, mouseY);
            disableScissor();
            renderScrollbar(context, contentX, contentY, contentWidth, contentHeight);
        }
    }

    /**
     * Рисует список элементов в правой панели.
     */
    private void renderItemList(DrawContext context, int listX, int listY, int listWidth, int listHeight, int mouseX, int mouseY) {
        float offsetPixels = (float) (smoothScrollOffset * ITEM_HEIGHT);
        int hoveredIndex = getHoveredIndex(mouseX, mouseY, listX, listY, offsetPixels);
        for (int i = 0; i < items.size(); i++) {
            int itemTop = (int) (listY + i * ITEM_HEIGHT - offsetPixels);
            int itemBottom = itemTop + ITEM_HEIGHT;
            if (itemBottom < listY || itemTop > listY + listHeight) continue;
            if (i == hoveredIndex) {
                context.fill(listX, itemTop, listX + listWidth, itemBottom, 0xFF444444);
            }
            context.drawTextWithShadow(textRenderer, Text.of(items.get(i)), listX + 4, itemTop + (ITEM_HEIGHT - textRenderer.fontHeight) / 2, 0xFFFFFF);
            context.fill(listX, itemBottom - 1, listX + listWidth, itemBottom, 0xFF2A2A2A);
        }
    }

    /**
     * Вычисляет индекс элемента, на который навели мышь.
     */
    private int getHoveredIndex(int mouseX, int mouseY, int listX, int listY, float offsetPixels) {
        int listContentWidth = RIGHT_PANEL_WIDTH - PANEL_PADDING * 2;
        if (mouseX < listX || mouseX > listX + listContentWidth) return -1;
        int relativeY = mouseY - listY;
        float correctedY = relativeY + offsetPixels;
        int index = (int) (correctedY / ITEM_HEIGHT);
        return (index < 0 || index >= items.size()) ? -1 : index;
    }

    /**
     * Рисует ползунок для списка в правой панели.
     */
    private void renderScrollbar(DrawContext context, int listX, int listY, int listWidth, int listHeight) {
        if (maxScrollOffset <= 0) return;
        int scrollbarHeight = Math.max(MIN_SCROLLBAR_HEIGHT,
                (int) ((listHeight * 1.0f / (items.size() * ITEM_HEIGHT)) * listHeight));
        int trackHeight = listHeight - scrollbarHeight;
        double scrollPercent = smoothScrollOffset / maxScrollOffset;
        int scrollbarY = listY + (int) (scrollPercent * trackHeight);
        int scrollbarX = listX + listWidth - SCROLLBAR_WIDTH;
        context.fill(scrollbarX - 1, listY, scrollbarX + SCROLLBAR_WIDTH + 1, listY + listHeight, 0xFF222222);
        context.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, 0xFFAAAAAA);
    }

    /**
     * Рисует детальную панель выбранного элемента с кнопкой "Back".
     */
    private void renderDetailPanel(DrawContext context, int panelX, int panelY, int panelWidth, int panelHeight) {
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF111111);
        int backWidth = 50;
        int backHeight = 20;
        int backX = panelX + PANEL_PADDING;
        int backY = panelY + panelHeight - backHeight - PANEL_PADDING;
        context.fill(backX, backY, backX + backWidth, backY + backHeight, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of("Back"), backX + 5, backY + 5, 0xFFFFFF);
        if (selectedItemIndex >= 0 && selectedItemIndex < items.size()) {
            String detailText = "Detail for " + items.get(selectedItemIndex);
            context.drawTextWithShadow(textRenderer, Text.of(detailText), panelX + PANEL_PADDING, panelY + PANEL_PADDING + 40, 0xFFFFFF);
        }
    }

    /**
     * Рисует контекстное меню для фоновой области.
     * Если contextBlock != null, меню содержит опцию "Удалить", иначе – только две опции.
     */
    private void renderBackgroundContextMenu(DrawContext context) {
        int menuX = backgroundContextMenuX;
        int menuY = backgroundContextMenuY;
        int options = (contextBlock != null) ? 3 : 2;
        context.fill(menuX, menuY, menuX + CONTEXT_MENU_WIDTH, menuY + CONTEXT_OPTION_HEIGHT * options, 0xFF333333);
        context.drawTextWithShadow(textRenderer, Text.of("Создать новый блок"), menuX + 4, menuY + 4, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.of("Создать триггер"), menuX + 4, menuY + CONTEXT_OPTION_HEIGHT + 4, 0xFFFFFF);
        if (contextBlock != null) {
            context.drawTextWithShadow(textRenderer, Text.of("Удалить"), menuX + 4, menuY + CONTEXT_OPTION_HEIGHT * 2 + 4, 0xFFFFFF);
        }
    }

    /**
     * Отрисовка оверлея для конфигурации (выбора JSON-файла).
     * Кнопки "Открыть" и "Удалить" располагаются рядом с кнопкой закрытия.
     * Также реализован плавный скролл списка файлов с собственным ползунком.
     */
    private void renderConfigOverlay(DrawContext context, int mouseX, int mouseY) {
        int overlayX = (width - configOverlayWidth) / 2;
        int overlayY = (height - configOverlayHeight) / 2;
        context.fill(overlayX, overlayY, overlayX + configOverlayWidth, overlayY + configOverlayHeight, 0xAA000000);
        context.fill(overlayX, overlayY, overlayX + configOverlayWidth, overlayY + 30, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of("Путь: " + CONFIG_FOLDER), overlayX + 5, overlayY + 8, 0xFFFFFF);
        int closeButtonSize = 20;
        int closeX = overlayX + configOverlayWidth - closeButtonSize - 5;
        int closeY = overlayY + 5;
        context.fill(closeX, closeY, closeX + closeButtonSize, closeY + closeButtonSize, 0xFFFF0000);
        context.drawTextWithShadow(textRenderer, Text.of("X"), closeX + 6, closeY + 4, 0xFFFFFF);
        // Кнопки "Открыть" и "Удалить" располагаем слева от крестика
        int buttonSize = 24;
        int spacing = 5;
        int openX = closeX - buttonSize - spacing;
        int openY = closeY;
        int deleteX = openX - buttonSize - spacing;
        int deleteY = closeY;
        context.drawTexture(OPEN_TEXTURE, openX, openY, 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
        context.drawTexture(DELETE_TEXTURE, deleteX, deleteY, 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);

        // Область списка файлов (начинается ниже заголовка)
        int listY = overlayY + 40;
        int listHeight = configOverlayHeight - 40;
        int lineHeight = 20;
        // Плавное скроллирование
        configSmoothScrollOffset += (configScrollOffset - configSmoothScrollOffset) * 0.2;

        // Включаем scissor для области списка
        context.enableScissor(overlayX, listY, configOverlayWidth - SCROLLBAR_WIDTH, listHeight);
        for (int i = 0; i < configFiles.size(); i++) {
            int fileY = listY + i * lineHeight - (int)(configSmoothScrollOffset * lineHeight);
            if (fileY + lineHeight < listY || fileY > listY + listHeight) continue;
            if (i == selectedConfigIndex) {
                context.fill(overlayX, fileY, overlayX + configOverlayWidth - SCROLLBAR_WIDTH, fileY + lineHeight, 0xFF666666);
            } else if (mouseX >= overlayX && mouseX <= overlayX + configOverlayWidth - SCROLLBAR_WIDTH &&
                    mouseY >= fileY && mouseY <= fileY + lineHeight) {
                context.fill(overlayX, fileY, overlayX + configOverlayWidth - SCROLLBAR_WIDTH, fileY + lineHeight, 0xFF555555);
            }
            context.drawTextWithShadow(textRenderer, Text.of(configFiles.get(i).getName()), overlayX + 5, fileY + 4, 0xFFFFFF);
        }
        context.disableScissor();

        // Отрисовка ползунка для списка файлов
        int visibleLines = (listHeight) / lineHeight;
        int maxConfigScroll = Math.max(0, configFiles.size() - visibleLines);
        if (maxConfigScroll > 0) {
            int scrollbarHeight = Math.max(MIN_SCROLLBAR_HEIGHT, (int)((float)visibleLines / configFiles.size() * listHeight));
            int trackHeight = listHeight - scrollbarHeight;
            double scrollPercent = configSmoothScrollOffset / maxConfigScroll;
            int scrollbarY = listY + (int)(scrollPercent * trackHeight);
            int scrollbarX = overlayX + configOverlayWidth - SCROLLBAR_WIDTH;
            context.fill(scrollbarX - 1, listY, scrollbarX + SCROLLBAR_WIDTH + 1, listY + listHeight, 0xFF222222);
            context.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, 0xFFAAAAAA);
        }
    }

    /**
     * Отрисовка оверлея для настройки триггера (для триггерных блоков).
     */
    private void renderInputOverlay(DrawContext context, int mouseX, int mouseY) {
        int overlayX = (width - inputOverlayWidth) / 2;
        int overlayY = (height - inputOverlayHeight) / 2;
        context.fill(overlayX, overlayY, overlayX + inputOverlayWidth, overlayY + inputOverlayHeight, 0xAA000000);
        context.fill(overlayX, overlayY, overlayX + inputOverlayWidth, overlayY + 30, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of("Настройка триггера"), overlayX + 5, overlayY + 8, 0xFFFFFF);
        int optionHeight = 20;
        int option1Y = overlayY + 40;
        int option2Y = option1Y + optionHeight + 10;
        if (mouseX >= overlayX && mouseX <= overlayX + inputOverlayWidth &&
                mouseY >= option1Y && mouseY < option1Y + optionHeight) {
            context.fill(overlayX, option1Y, overlayX + inputOverlayWidth, option1Y + optionHeight, 0xFF555555);
        }
        context.drawTextWithShadow(textRenderer, Text.of("1: Безусловный"), overlayX + 5, option1Y + 4, 0xFFFFFF);
        if (mouseX >= overlayX && mouseX <= overlayX + inputOverlayWidth &&
                mouseY >= option2Y && mouseY < option2Y + optionHeight) {
            context.fill(overlayX, option2Y, overlayX + inputOverlayWidth, option2Y + optionHeight, 0xFF555555);
        }
        context.drawTextWithShadow(textRenderer, Text.of("2: Нажатие клавиши"), overlayX + 5, option2Y + 4, 0xFFFFFF);
        if (activeInputBlock != null && activeInputBlock.triggerType == 2) {
            int textFieldY = option2Y + optionHeight + 10;
            int textFieldHeight = 20;
            context.fill(overlayX + 5, textFieldY, overlayX + inputOverlayWidth - 5, textFieldY + textFieldHeight, 0xFF333333);
            String input = activeInputBlock.triggerInput == null ? "" : activeInputBlock.triggerInput;
            context.drawTextWithShadow(textRenderer, Text.of(input), overlayX + 10, textFieldY + 4, 0xFFFFFF);
        }
        int confirmWidth = 50, confirmHeight = 20;
        int confirmX = overlayX + inputOverlayWidth - confirmWidth - 10;
        int confirmY = overlayY + inputOverlayHeight - confirmHeight - 10;
        context.fill(confirmX, confirmY, confirmX + confirmWidth, confirmY + confirmHeight, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of("ОК"), confirmX + 15, confirmY + 4, 0xFFFFFF);
    }

    /**
     * Отрисовка оверлея для настройки действий (для обычных блоков).
     * Здесь предлагается два варианта: "1: Шок" и "2: Барьер" (без текстового поля).
     */
    private void renderActionOverlay(DrawContext context, int mouseY) {
        int overlayX = (width - inputOverlayWidth) / 2;
        int overlayY = (height - inputOverlayHeight) / 2;
        context.fill(overlayX, overlayY, overlayX + inputOverlayWidth, overlayY + inputOverlayHeight, 0xAA000000);
        context.fill(overlayX, overlayY, overlayX + inputOverlayWidth, overlayY + 30, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of("Настройка действий"), overlayX + 5, overlayY + 8, 0xFFFFFF);
        int optionHeight = 20;
        int option1Y = overlayY + 40;
        int option2Y = option1Y + optionHeight + 10;
        if (mouseY >= option1Y && mouseY < option1Y + optionHeight) {
            context.fill(overlayX, option1Y, overlayX + inputOverlayWidth, option1Y + optionHeight, 0xFF555555);
        }
        context.drawTextWithShadow(textRenderer, Text.of("1: Шок"), overlayX + 5, option1Y + 4, 0xFFFFFF);
        if (mouseY >= option2Y && mouseY < option2Y + optionHeight) {
            context.fill(overlayX, option2Y, overlayX + inputOverlayWidth, option2Y + optionHeight, 0xFF555555);
        }
        context.drawTextWithShadow(textRenderer, Text.of("2: Барьер"), overlayX + 5, option2Y + 4, 0xFFFFFF);
        int confirmWidth = 50, confirmHeight = 20;
        int confirmX = overlayX + inputOverlayWidth - confirmWidth - 10;
        int confirmY = overlayY + inputOverlayHeight - confirmHeight - 10;
        context.fill(confirmX, confirmY, confirmX + confirmWidth, confirmY + confirmHeight, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of("ОК"), confirmX + 15, confirmY + 4, 0xFFFFFF);
    }

    /**
     * Загружает список JSON-файлов из папки с конфигами.
     */
    private void loadConfigFiles() {
        File folder = new File(CONFIG_FOLDER);
        configFiles.clear();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    configFiles.add(f);
                }
            }
        }
    }

    /**
     * Сохраняет сцену (блоки, триггеры и соединения) в новый JSON-файл.
     */
    private void saveScene() {
        SceneData sceneData = new SceneData();
        sceneData.blocks = new ArrayList<>(backgroundBlocks);
        sceneData.connections = new ArrayList<>();
        for (Connection conn : connections) {
            ConnectionData cd = new ConnectionData();
            cd.startId = conn.start.id;
            cd.endId = conn.end.id;
            cd.particleProgress = conn.particleProgress;
            cd.speed = conn.speed;
            sceneData.connections.add(cd);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(sceneData);
        long time = System.currentTimeMillis();
        String filename = "scene_" + time + ".json";
        File configFile = new File(CONFIG_FOLDER, filename);
        configFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает сцену (блоки, триггеры и соединения) из указанного JSON-файла.
     */
    private void loadScene(File file) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            SceneData sceneData = gson.fromJson(reader, SceneData.class);
            if (sceneData != null) {
                backgroundBlocks.clear();
                connections.clear();
                backgroundBlocks.addAll(sceneData.blocks);
                Map<Integer, CustomBlock> blockMap = new HashMap<>();
                for (CustomBlock block : backgroundBlocks) {
                    blockMap.put(block.id, block);
                }
                for (ConnectionData cd : sceneData.connections) {
                    CustomBlock startBlock = blockMap.get(cd.startId);
                    CustomBlock endBlock = blockMap.get(cd.endId);
                    if (startBlock != null && endBlock != null) {
                        Connection conn = new Connection(startBlock, endBlock);
                        conn.particleProgress = cd.particleProgress;
                        conn.speed = cd.speed;
                        connections.add(conn);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Включает scissor-тест для заданной области.
     */
    private void enableScissor(int x, int y, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        double scaleFactor = window.getScaleFactor();
        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) ((this.height - (y + height)) * scaleFactor);
        int scissorW = (int) (width * scaleFactor);
        int scissorH = (int) (height * scaleFactor);
        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
    }

    private void disableScissor() {
        RenderSystem.disableScissor();
    }

    // ========================
    // Обработка событий мыши и клавиатуры
    // ========================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Если оверлей конфигов активен, обрабатываем его
        if (configOverlayActive) {
            int overlayX = (width - configOverlayWidth) / 2;
            int overlayY = (height - configOverlayHeight) / 2;
            int closeButtonSize = 20;
            int closeX = overlayX + configOverlayWidth - closeButtonSize - 5;
            int closeY = overlayY + 5;
            if (mouseX >= closeX && mouseX <= closeX + closeButtonSize &&
                    mouseY >= closeY && mouseY <= closeY + closeButtonSize) {
                configOverlayActive = false;
                selectedConfigIndex = -1;
                return true;
            }
            int startY = overlayY + 40;
            int lineHeight = 20;
            // Обработка клика по списку файлов
            if (mouseX >= overlayX && mouseX <= overlayX + configOverlayWidth &&
                    mouseY >= startY && mouseY <= startY + lineHeight * configFiles.size()) {
                int index = (int) Math.floor((mouseY - startY + configSmoothScrollOffset * lineHeight) / lineHeight);
                selectedConfigIndex = index;
                return true;
            }
            int buttonSize = 24;
            int spacing = 5;
            int closeXPos = overlayX + configOverlayWidth - closeButtonSize - 5;
            int openX = closeXPos - buttonSize - spacing;
            int deleteX = openX - buttonSize - spacing;
            int btnY = overlayY + 5;
            if (mouseX >= openX && mouseX <= openX + buttonSize &&
                    mouseY >= btnY && mouseY <= btnY + buttonSize) {
                if (selectedConfigIndex >= 0 && selectedConfigIndex < configFiles.size()) {
                    File selectedFile = configFiles.get(selectedConfigIndex);
                    loadScene(selectedFile);
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                                Text.of("Конфиг загружен: " + selectedFile.getName()), false);
                    }
                    configOverlayActive = false;
                    selectedConfigIndex = -1;
                }
                return true;
            }
            if (mouseX >= deleteX && mouseX <= deleteX + buttonSize &&
                    mouseY >= btnY && mouseY <= btnY + buttonSize) {
                if (selectedConfigIndex >= 0 && selectedConfigIndex < configFiles.size()) {
                    File toDelete = configFiles.get(selectedConfigIndex);
                    if (toDelete.delete()) {
                        configFiles.remove(selectedConfigIndex);
                        selectedConfigIndex = -1;
                        if (MinecraftClient.getInstance().player != null) {
                            MinecraftClient.getInstance().player.sendMessage(
                                    Text.of("Файл удалён: " + toDelete.getName()), false);
                        }
                    }
                }
                return true;
            }
            return true;
        }

        // Если активен оверлей настройки триггера, обрабатываем его
        if (inputOverlayActive) {
            int overlayX = (width - inputOverlayWidth) / 2;
            int overlayY = (height - inputOverlayHeight) / 2;
            int optionHeight = 20;
            int option1Y = overlayY + 40;
            int option2Y = option1Y + optionHeight + 10;
            if (mouseX >= overlayX && mouseX <= overlayX + inputOverlayWidth &&
                    mouseY >= option1Y && mouseY < option1Y + optionHeight) {
                if (activeInputBlock != null) {
                    activeInputBlock.triggerType = 1;
                    activeInputBlock.triggerInput = "";
                }
                inputOverlayActive = false;
                activeInputBlock = null;
                return true;
            }
            if (mouseX >= overlayX && mouseX <= overlayX + inputOverlayWidth &&
                    mouseY >= option2Y && mouseY < option2Y + optionHeight) {
                if (activeInputBlock != null) {
                    activeInputBlock.triggerType = 2;
                }
                return true;
            }
            int confirmWidth = 50, confirmHeight = 20;
            int confirmX = overlayX + inputOverlayWidth - confirmWidth - 10;
            int confirmY = overlayY + inputOverlayHeight - confirmHeight - 10;
            if (mouseX >= confirmX && mouseX < confirmX + confirmWidth &&
                    mouseY >= confirmY && mouseY < confirmY + confirmHeight) {
                if (activeInputBlock != null && activeInputBlock.triggerType == 2 &&
                        activeInputBlock.triggerInput != null && !activeInputBlock.triggerInput.isEmpty()) {
                    activeInputBlock.inputText = "Press: " + activeInputBlock.triggerInput;
                }
                inputOverlayActive = false;
                activeInputBlock = null;
                return true;
            }
            return true;
        }

        // Если активен оверлей настройки действий, обрабатываем его
        if (actionOverlayActive) {
            int overlayX = (width - inputOverlayWidth) / 2;
            int overlayY = (height - inputOverlayHeight) / 2;
            int optionHeight = 20;
            int option1Y = overlayY + 40;
            int option2Y = option1Y + optionHeight + 10;
            if (mouseX >= overlayX && mouseX <= overlayX + inputOverlayWidth &&
                    mouseY >= option1Y && mouseY < option1Y + optionHeight) {
                if (activeActionBlock != null) {
                    activeActionBlock.actionType = 1;
                    activeActionBlock.actionInput = "";
                }
                actionOverlayActive = false;
                activeActionBlock = null;
                return true;
            }
            if (mouseX >= overlayX && mouseX <= overlayX + inputOverlayWidth &&
                    mouseY >= option2Y && mouseY < option2Y + optionHeight) {
                if (activeActionBlock != null) {
                    activeActionBlock.actionType = 2;
                }
                return true;
            }
            int confirmWidth = 50, confirmHeight = 20;
            int confirmX = overlayX + inputOverlayWidth - confirmWidth - 10;
            int confirmY = overlayY + inputOverlayHeight - confirmHeight - 10;
            if (mouseX >= confirmX && mouseX < confirmX + confirmWidth &&
                    mouseY >= confirmY && mouseY < confirmY + confirmHeight) {
                if (activeActionBlock != null) {
                    if (activeActionBlock.actionType == 1) {
                        activeActionBlock.inputText = "Action: Шок";
                    } else if (activeActionBlock.actionType == 2) {
                        activeActionBlock.inputText = "Action: Барьер";
                    }
                }
                actionOverlayActive = false;
                activeActionBlock = null;
                return true;
            }
            return true;
        }

        // Обработка кликов в фоновой области (исключая правую панель)
        int backgroundAreaX1 = 0;
        int backgroundAreaX2 = width - RIGHT_PANEL_WIDTH;
        if (mouseX >= backgroundAreaX1 && mouseX <= backgroundAreaX2) {
            // Обработка двойного клика по области Input блока
            if (button == 0) {
                for (int i = backgroundBlocks.size() - 1; i >= 0; i--) {
                    CustomBlock block = backgroundBlocks.get(i);
                    int screenX = (int) ((block.x + offsetX) * scale + width / 2);
                    int screenY = (int) ((block.y + offsetY) * scale + height / 2);
                    int blockScreenWidth = (int) (block.width * scale);
                    int blockScreenHeight = (int) (block.height * scale);
                    int inputRegionY = screenY + blockScreenHeight - INPUT_AREA_HEIGHT;
                    if (mouseX >= screenX && mouseX <= screenX + blockScreenWidth &&
                            mouseY >= inputRegionY && mouseY <= screenY + blockScreenHeight) {
                        long now = System.currentTimeMillis();
                        if (block.isTrigger) {
                            if (lastInputClickedBlock == block && now - lastInputClickTime < 300) {
                                activeInputBlock = block;
                                inputOverlayActive = true;
                            }
                            lastInputClickTime = now;
                            lastInputClickedBlock = block;
                        } else {
                            if (lastActionClickedBlock == block && now - lastActionClickTime < 300) {
                                activeActionBlock = block;
                                actionOverlayActive = true;
                            }
                            lastActionClickTime = now;
                            lastActionClickedBlock = block;
                        }
                        return true;
                    }
                }
            }
            // Обработка правого клика (ПКМ)
            if (button == 1) {
                CustomBlock clickedBlock = null;
                for (int i = backgroundBlocks.size() - 1; i >= 0; i--) {
                    CustomBlock block = backgroundBlocks.get(i);
                    int screenX = (int) ((block.x + offsetX) * scale + width / 2);
                    int screenY = (int) ((block.y + offsetY) * scale + height / 2);
                    int blockScreenWidth = (int) (block.width * scale);
                    int blockScreenHeight = (int) (block.height * scale);
                    if (mouseX >= screenX && mouseX <= screenX + blockScreenWidth &&
                            mouseY >= screenY && mouseY <= screenY + blockScreenHeight) {
                        clickedBlock = block;
                        break;
                    }
                }
                if (Screen.hasShiftDown()) {
                    if (clickedBlock != null) {
                        if (connectionStartBlock == null) {
                            connectionStartBlock = clickedBlock;
                            if (MinecraftClient.getInstance().player != null) {
                                MinecraftClient.getInstance().player.sendMessage(
                                        Text.of("Начало соединения установлено"), false);
                            }
                        } else if (connectionStartBlock != clickedBlock) {
                            connections.add(new Connection(connectionStartBlock, clickedBlock));
                            if (MinecraftClient.getInstance().player != null) {
                                MinecraftClient.getInstance().player.sendMessage(
                                        Text.of("Соединение создано"), false);
                            }
                            connectionStartBlock = null;
                        }
                    }
                    return true;
                } else {
                    contextBlock = clickedBlock;
                    backgroundContextMenuActive = true;
                    backgroundContextMenuX = (int) mouseX;
                    backgroundContextMenuY = (int) mouseY;
                    return true;
                }
            }
            if (backgroundContextMenuActive && button == 0) {
                int menuX = backgroundContextMenuX;
                int menuY = backgroundContextMenuY;
                if (mouseX >= menuX && mouseX <= menuX + CONTEXT_MENU_WIDTH &&
                        mouseY >= menuY && mouseY < menuY + CONTEXT_OPTION_HEIGHT) {
                    double worldX = (mouseX - width / 2d) / scale - offsetX;
                    double worldY = (mouseY - height / 2d) / scale - offsetY;
                    CustomBlock newBlock = new CustomBlock((int) worldX, (int) worldY, false);
                    backgroundBlocks.add(newBlock);
                    backgroundContextMenuActive = false;
                    contextBlock = null;
                    return true;
                }
                if (mouseX >= menuX && mouseX <= menuX + CONTEXT_MENU_WIDTH &&
                        mouseY >= menuY + CONTEXT_OPTION_HEIGHT && mouseY < menuY + CONTEXT_OPTION_HEIGHT * 2) {
                    double worldX = (mouseX - width / 2d) / scale - offsetX;
                    double worldY = (mouseY - height / 2d) / scale - offsetY;
                    CustomBlock newTrigger = new CustomBlock((int) worldX, (int) worldY, true);
                    backgroundBlocks.add(newTrigger);
                    backgroundContextMenuActive = false;
                    contextBlock = null;
                    return true;
                }
                if (contextBlock != null &&
                        mouseX >= menuX && mouseX <= menuX + CONTEXT_MENU_WIDTH &&
                        mouseY >= menuY + CONTEXT_OPTION_HEIGHT * 2 && mouseY < menuY + CONTEXT_OPTION_HEIGHT * 3) {
                    connections.removeIf(conn -> conn.start == contextBlock || conn.end == contextBlock);
                    backgroundBlocks.remove(contextBlock);
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                                Text.of("Блок удалён"), false);
                    }
                    backgroundContextMenuActive = false;
                    contextBlock = null;
                    return true;
                }
                backgroundContextMenuActive = false;
            }
        }

        // Обработка кликов по правой панели
        int panelX = width - RIGHT_PANEL_WIDTH;
        int diskSize = 16;
        int diskX = panelX + RIGHT_PANEL_WIDTH - PANEL_PADDING - diskSize;
        int diskY = PANEL_PADDING;
        int folderSize = 16;
        int folderX = diskX - PANEL_PADDING - folderSize;
        int folderY = PANEL_PADDING;
        if (button == 0) {
            if (mouseX >= diskX && mouseX <= diskX + diskSize &&
                    mouseY >= diskY && mouseY <= diskY + diskSize) {
                saveScene();
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                            Text.of("Конфиг сцены сохранён!"), false);
                }
                return true;
            }
            if (mouseX >= folderX && mouseX <= folderX + folderSize &&
                    mouseY >= folderY && mouseY <= folderY + folderSize) {
                configOverlayActive = true;
                loadConfigFiles();
                selectedConfigIndex = -1;
                // Сброс скролла проводника
                configScrollOffset = 0;
                configSmoothScrollOffset = 0;
                return true;
            }
            if (!detailActive) {
                int contentX = panelX + PANEL_PADDING;
                int contentY = PANEL_PADDING + 40;
                int contentWidth = RIGHT_PANEL_WIDTH - PANEL_PADDING * 2;
                int contentHeight = height - (contentY + PANEL_PADDING);
                if (mouseX >= contentX && mouseX <= contentX + contentWidth &&
                        mouseY >= contentY && mouseY <= contentY + contentHeight) {
                    float offsetPixels = (float) (smoothScrollOffset * ITEM_HEIGHT);
                    int relativeY = (int) (mouseY - contentY);
                    int index = (int) ((relativeY + offsetPixels) / ITEM_HEIGHT);
                    if (index >= 0 && index < items.size()) {
                        detailActive = true;
                        selectedItemIndex = index;
                        return true;
                    }
                }
            } else {
                int backWidth = 50;
                int backHeight = 20;
                int backX = panelX + PANEL_PADDING;
                int backY = height - backHeight - PANEL_PADDING;
                if (mouseX >= backX && mouseX <= backX + backWidth &&
                        mouseY >= backY && mouseY <= backY + backHeight) {
                    detailActive = false;
                    selectedItemIndex = -1;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && !detailActive && !configOverlayActive && !inputOverlayActive && !actionOverlayActive) {
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
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // Если конфиг-оверлей активен, обрабатываем скроллинг для проводника
        if (configOverlayActive) {
            int overlayX = (width - configOverlayWidth) / 2;
            int overlayY = (height - configOverlayHeight) / 2;
            // Область списка файлов начинается с overlayY + 40 и имеет высоту configOverlayHeight - 40
            if (mouseX >= overlayX && mouseX <= overlayX + configOverlayWidth &&
                    mouseY >= overlayY + 40 && mouseY <= overlayY + configOverlayHeight) {
                int visibleLines = (configOverlayHeight - 40) / 20;
                int maxConfigScroll = Math.max(0, configFiles.size() - visibleLines);
                configScrollOffset = Math.max(0, Math.min(configScrollOffset - (int)(amount * 3), maxConfigScroll));
                return true;
            }
        }
        int panelX = width - RIGHT_PANEL_WIDTH;
        if (!detailActive && mouseX >= panelX && mouseX <= panelX + RIGHT_PANEL_WIDTH) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)(amount * SCROLL_SPEED), maxScrollOffset));
        } else {
            double oldTargetScale = targetScale;
            targetScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, targetScale + amount * 0.2));
            focusX = (mouseX - width / 2) / scale + offsetX;
            focusY = (mouseY - height / 2) / scale + offsetY;
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (inputOverlayActive && activeInputBlock != null && activeInputBlock.triggerType == 2) {
            activeInputBlock.triggerInput = (activeInputBlock.triggerInput == null ? "" : activeInputBlock.triggerInput) + chr;
            return true;
        }
        if (actionOverlayActive && activeActionBlock != null && activeActionBlock.actionType == 2) {
            activeActionBlock.actionInput = (activeActionBlock.actionInput == null ? "" : activeActionBlock.actionInput) + chr;
            return true;
        }
        return super.charTyped(chr, keyCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inputOverlayActive && activeInputBlock != null && activeInputBlock.triggerType == 2) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && activeInputBlock.triggerInput != null && activeInputBlock.triggerInput.length() > 0) {
                activeInputBlock.triggerInput = activeInputBlock.triggerInput.substring(0, activeInputBlock.triggerInput.length() - 1);
                return true;
            }
        }
        if (actionOverlayActive && activeActionBlock != null && activeActionBlock.actionType == 2) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && activeActionBlock.actionInput != null && activeActionBlock.actionInput.length() > 0) {
                activeActionBlock.actionInput = activeActionBlock.actionInput.substring(0, activeActionBlock.actionInput.length() - 1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        super.close();
    }

    /**
     * Класс для кастомного блока (или триггера).
     * Для триггеров используются поля triggerType и triggerInput,
     * для обычных блоков – поля actionType и actionInput.
     */
    private static class CustomBlock {
        int id;
        int x, y, width, height;
        String blockText;
        String inputText;
        boolean isTrigger;
        // Для триггеров
        int triggerType;       // 0 – не задано, 1: Безусловный, 2: Нажатие клавиши
        String triggerInput;
        // Для обычных блоков (не триггеров)
        int actionType;        // 0 – не задано, 1: Шок, 2: Барьер
        String actionInput;

        private static int nextId = 0;

        CustomBlock(int x, int y) {
            this(x, y, false);
        }

        CustomBlock(int x, int y, boolean isTrigger) {
            this.id = nextId++;
            this.x = x;
            this.y = y;
            this.width = 50;
            this.height = 50;
            this.isTrigger = isTrigger;
            this.blockText = isTrigger ? "Trigger" : "Block";
            this.inputText = "";
            this.triggerType = 0;
            this.triggerInput = "";
            this.actionType = 0;
            this.actionInput = "";
        }
    }

    /**
     * Класс для соединения двух блоков/триггеров.
     */
    private static class Connection {
        CustomBlock start;
        CustomBlock end;
        float particleProgress;
        float speed;
        float smoothParticleX;
        float smoothParticleY;
        boolean smoothingInitialized = false;

        Connection(CustomBlock start, CustomBlock end) {
            this.start = start;
            this.end = end;
            this.particleProgress = 0f;
            this.speed = 0.5f;
        }
    }

    /**
     * Класс для сериализации данных соединения.
     */
    private static class ConnectionData {
        int startId;
        int endId;
        float particleProgress;
        float speed;
    }

    /**
     * Класс для сериализации сцены.
     */
    private static class SceneData {
        List<CustomBlock> blocks;
        List<ConnectionData> connections;
    }
}
