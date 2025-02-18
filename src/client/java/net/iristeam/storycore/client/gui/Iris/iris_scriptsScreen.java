package net.iristeam.storycore.client.gui.Iris;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MatrixUtil;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


import java.io.File;
import java.io.FileReader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class iris_scriptsScreen extends Screen {

    // Текстуры
    private static final Identifier STAR_TEXTURE = new Identifier("storycore", "textures/gui/star.png");
    private static final Identifier PANEL_TEXTURE = new Identifier("storycore", "textures/gui/left_menu_iris.png");
    private static final Identifier DISK_TEXTURE = new Identifier("storycore", "textures/gui/disk.png");
    private static final Identifier FOLDER_TEXTURE = new Identifier("storycore", "textures/gui/folder.png");
    // Текстуры для кнопок оверлея
    private static final Identifier OPEN_TEXTURE = new Identifier("storycore", "textures/gui/open.png");
    private static final Identifier DELETE_TEXTURE = new Identifier("storycore", "textures/gui/delete.png");
    // Текстура для частицы, движущейся по соединению
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
    private static final double SCROLL_SMOOTHNESS = 0.2;

    private final List<String> items = List.of(
            "Элемент 1", "Элемент 2", "Элемент 3", "Элемент 4", "Элемент 5",
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
    // Ссылка на блок (или триггер), по которому был совершен правый клик (если таковой есть)
    private CustomBlock contextBlock = null;

    // Обработка двойного клика по тексту "input" у триггера
    private long lastInputClickTime = 0;
    private CustomBlock lastClickedTriggerBlock = null;
    // Флаг отображения панели выбора триггера и связанные с ней поля
    private boolean inputPanelActive = false;
    // Ссылка на триггер, для которого открыта панель выбора
    private CustomBlock selectedTriggerBlock = null;
    // Индекс выбранного элемента из списка триггеров
    private int selectedTriggerIndex = -1;
    // Список доступных триггеров для выбора
    private final List<String> availableTriggers = List.of("Trigger A", "Trigger B", "Trigger C", "Trigger D", "Trigger E", "Trigger F");
    // Параметры прокрутки для списка в панели выбора
    private int inputListScrollOffset = 0;
    private double inputListSmoothScrollOffset = 0.0;
    private int maxInputListScrollOffset = 0;

    // Пути для конфигов – создаётся папка "config/Storycore"
    private static final String CONFIG_FOLDER = "config/Storycore";

    // Параметры для модального окна (оверлея) конфигов
    private boolean configOverlayActive = false;
    private List<File> configFiles = new ArrayList<>();
    private final int configOverlayWidth = 300;
    private final int configOverlayHeight = 200;
    // Для выделения выбранного файла в оверлее
    private int selectedConfigIndex = -1;
    // Параметры прокрутки списка файлов
    private int configListScrollOffset = 0;
    private double configListSmoothScrollOffset = 0.0;
    private int maxConfigListScrollOffset = 0;

    // Поле для хранения зомби (рендерится статически)
    private ZombieEntity zombieEntity;

    public iris_scriptsScreen() {
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

        // Попытка загрузки блоков (и связей) из файла по умолчанию
        loadBlocks();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableBlend();

        // Если активен оверлей конфигов, отрисовываем его и выходим
        if (configOverlayActive) {
            context.fill(0, 0, width, height, 0xFF2A2A2A);
            renderConfigOverlay(context, mouseX, mouseY);
            return;
        }

        // Если активна панель выбора триггера, отрисовываем её поверх всего (и скрываем основной GUI)
        if (inputPanelActive) {
            context.fill(0, 0, width, height, 0xFF222222);
            renderInputPanel(context, mouseX, mouseY);
            super.render(context, mouseX, mouseY, delta);
            return;
        }

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
        enableScissor(0, 0, width - RIGHT_PANEL_WIDTH, height);
        renderConnectionLines(context);
        renderBackgroundBlocks(context);
        renderConnectionParticles(context, delta);
        disableScissor();
        renderRightPanel(context, mouseX, mouseY);
        if (backgroundContextMenuActive) {
            renderBackgroundContextMenu(context);
        }
        super.render(context, mouseX, mouseY, delta);

        // Рендерим зомби в левом нижнем углу (без отслеживания мыши)
        renderZombieEntity(context);
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
     * Рисует блоки (и триггеры) на фоне.
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
            context.drawTextWithShadow(textRenderer,
                    Text.of(block.inputText.isEmpty() ? "input" : block.inputText),
                    screenX + 6, tfScreenY + 2, 0xFFFFFF);
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
     * Отрисовывает частицы, движущиеся по соединениям, с более плавной анимацией.
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

            double t = conn.particleProgress;
            double easedT = 3 * t * t - 2 * t * t * t; // ease-in-out

            int particleX = startX + (int) ((endX - startX) * easedT);
            int particleY = startY + (int) ((endY - startY) * easedT);

            double dx = endX - startX;
            double dy = endY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                double amplitude = 2;
                double oscillation = amplitude * Math.sin(conn.particleProgress * Math.PI * 2 * 3);
                double perpX = -dy / distance;
                double perpY = dx / distance;
                particleX += (int) (perpX * oscillation);
                particleY += (int) (perpY * oscillation);
            }

            int particleSize = 8;
            context.drawTexture(PARTICLE_TEXTURE, particleX - particleSize / 2, particleY - particleSize / 2,
                    0, 0, particleSize, particleSize, 16, 16);
        }
    }

    /**
     * Рисует правую панель с заголовком и кнопками.
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
     * Вычисляет индекс элемента, на который навели мышь в правой панели.
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
     * Рисует ползунок для правой панели.
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
     * Рисует детальную панель для выбранного элемента с кнопкой Back.
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
     * Отрисовывает панель выбора триггера (input-панель) с полным затемнением фона и ползунком для списка.
     */
    private void renderInputPanel(DrawContext context, int mouseX, int mouseY) {
        int panelWidth = 200;
        int panelHeight = 150;
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;
        context.fill(0, 0, width, height, 0xFF222222);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF222222);
        context.drawTextWithShadow(textRenderer, Text.of("Выберите триггер:"), panelX + 10, panelY + 10, 0xFFFFFF);

        int itemHeight = 20;
        int listAreaY = panelY + 30;
        int listAreaHeight = panelHeight - 30 - 40;
        maxInputListScrollOffset = Math.max(0, availableTriggers.size() * itemHeight - listAreaHeight);
        inputListSmoothScrollOffset += (inputListScrollOffset - inputListSmoothScrollOffset) * 0.2;

        enableScissor(panelX + 10, listAreaY, panelWidth - 20, listAreaHeight);
        for (int i = 0; i < availableTriggers.size(); i++) {
            int itemY = listAreaY + i * itemHeight - (int) inputListSmoothScrollOffset;
            if (itemY + itemHeight < listAreaY || itemY > listAreaY + listAreaHeight) continue;
            if (i == selectedTriggerIndex) {
                context.fill(panelX + 10, itemY, panelX + panelWidth - 10, itemY + itemHeight, 0xFF555555);
            }
            context.drawTextWithShadow(textRenderer, Text.of(availableTriggers.get(i)), panelX + 12, itemY + 3, 0xFFFFFF);
        }
        disableScissor();

        if (maxInputListScrollOffset > 0) {
            int scrollbarWidth = 6;
            int trackHeight = listAreaHeight;
            int scrollbarHeight = Math.max(20, (int) ((float) trackHeight / (availableTriggers.size() * itemHeight) * trackHeight));
            double scrollPercent = (double) inputListSmoothScrollOffset / maxInputListScrollOffset;
            int scrollbarY = listAreaY + (int) ((trackHeight - scrollbarHeight) * scrollPercent);
            int scrollbarX = panelX + panelWidth - 10 - scrollbarWidth;
            context.fill(scrollbarX - 1, listAreaY, scrollbarX + scrollbarWidth + 1, listAreaY + listAreaHeight, 0xFF111111);
            context.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0xFFAAAAAA);
        }

        String applyText = "Применить";
        int btnWidth = textRenderer.getWidth(applyText) + 10;
        int btnHeight = 20;
        int btnX = panelX + panelWidth - btnWidth - 10;
        int btnY = panelY + panelHeight - btnHeight - 10;
        context.fill(btnX, btnY, btnX + btnWidth, btnY + btnHeight, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of(applyText), btnX + 5, btnY + 5, 0xFFFFFF);
    }

    /**
     * Загружает блоки и связи из файла.
     */
    private void loadBlocksFromFile(File file) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            SavedData data = gson.fromJson(reader, SavedData.class);
            if (data != null) {
                backgroundBlocks.clear();
                backgroundBlocks.addAll(data.blocks);
                connections.clear();
                if (data.connections != null) {
                    for (ConnectionData cd : data.connections) {
                        if (cd.startIndex >= 0 && cd.startIndex < backgroundBlocks.size() &&
                                cd.endIndex >= 0 && cd.endIndex < backgroundBlocks.size()) {
                            Connection conn = new Connection(
                                    backgroundBlocks.get(cd.startIndex),
                                    backgroundBlocks.get(cd.endIndex)
                            );
                            conn.particleProgress = cd.particleProgress;
                            conn.speed = cd.speed;
                            connections.add(conn);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    // Обработка событий мыши
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Обработка панели выбора триггера
        if (inputPanelActive) {
            int panelWidth = 200;
            int panelHeight = 150;
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            int listAreaX = panelX + 10;
            int listAreaY = panelY + 30;
            int listAreaWidth = panelWidth - 20;
            int listAreaHeight = panelHeight - 30 - 40;
            if (mouseX >= listAreaX && mouseX <= listAreaX + listAreaWidth &&
                    mouseY >= listAreaY && mouseY <= listAreaY + listAreaHeight) {
                int index = (int) ((mouseY - listAreaY + inputListScrollOffset) / 20);
                if (index >= 0 && index < availableTriggers.size()) {
                    selectedTriggerIndex = index;
                }
                return true;
            }
            String applyText = "Применить";
            int btnWidth = textRenderer.getWidth(applyText) + 10;
            int btnHeight = 20;
            int btnX = panelX + panelWidth - btnWidth - 10;
            int btnY = panelY + panelHeight - btnHeight - 10;
            if (mouseX >= btnX && mouseX <= btnX + btnWidth &&
                    mouseY >= btnY && mouseY <= btnY + btnHeight) {
                if (selectedTriggerIndex != -1 && selectedTriggerBlock != null) {
                    selectedTriggerBlock.inputText = availableTriggers.get(selectedTriggerIndex);
                }
                inputPanelActive = false;
                selectedTriggerBlock = null;
                selectedTriggerIndex = -1;
                inputListScrollOffset = 0;
                inputListSmoothScrollOffset = 0;
                return true;
            }
            return true;
        }

        // Обработка оверлея конфигов
        if (configOverlayActive) {
            int overlayX = (width - configOverlayWidth) / 2;
            int overlayY = (height - configOverlayHeight) / 2;
            // Кнопка закрытия
            int closeButtonSize = 20;
            int closeX = overlayX + configOverlayWidth - closeButtonSize - 5;
            int closeY = overlayY + 5;
            if (mouseX >= closeX && mouseX <= closeX + closeButtonSize &&
                    mouseY >= closeY && mouseY <= closeY + closeButtonSize) {
                configOverlayActive = false;
                selectedConfigIndex = -1;
                return true;
            }
            // Обработка выбора элемента из списка
            int listAreaY = overlayY + 40;
            int lineHeight = 20;
            if (mouseX >= overlayX && mouseX <= overlayX + configOverlayWidth &&
                    mouseY >= listAreaY && mouseY <= listAreaY + configFiles.size() * lineHeight) {
                int index = (int) ((mouseY - listAreaY + configListScrollOffset) / lineHeight);
                if (index >= 0 && index < configFiles.size()) {
                    selectedConfigIndex = index;
                }
                return true;
            }
            // Обработка кнопок "Открыть" и "Удалить" на верхней панели
            int buttonSize = 24;
            int closeMargin = 5;
            int openX = overlayX + configOverlayWidth - closeButtonSize - closeMargin - buttonSize - 5;
            int openY = overlayY + (30 - buttonSize) / 2;
            int deleteX = openX - buttonSize - 5;
            int deleteY = openY;
            if (mouseX >= openX && mouseX <= openX + buttonSize &&
                    mouseY >= openY && mouseY <= openY + buttonSize) {
                if (selectedConfigIndex >= 0 && selectedConfigIndex < configFiles.size()) {
                    File selectedFile = configFiles.get(selectedConfigIndex);
                    loadBlocksFromFile(selectedFile);
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
                    mouseY >= deleteY && mouseY <= deleteY + buttonSize) {
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
                saveBlocks();
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                            Text.of("Конфиг плашек сохранён!"), false);
                }
                return true;
            }
            if (mouseX >= folderX && mouseX <= folderX + folderSize &&
                    mouseY >= folderY && mouseY <= folderY + folderSize) {
                configOverlayActive = true;
                loadConfigFiles();
                selectedConfigIndex = -1;
                configListScrollOffset = 0;
                configListSmoothScrollOffset = 0;
                return true;
            }
        }

        if (button == 0) {
            int rightPanelX = width - RIGHT_PANEL_WIDTH;
            if (detailActive) {
                int backWidth = 50;
                int backHeight = 20;
                int backX = rightPanelX + PANEL_PADDING;
                int backY = height - backHeight - PANEL_PADDING;
                if (mouseX >= backX && mouseX <= backX + backWidth &&
                        mouseY >= backY && mouseY <= backY + backHeight) {
                    detailActive = false;
                    selectedItemIndex = -1;
                    return true;
                }
            } else {
                int contentX = rightPanelX + PANEL_PADDING;
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
            }
        }

        int backgroundAreaX1 = 0;
        int backgroundAreaX2 = width - RIGHT_PANEL_WIDTH;
        if (mouseX >= backgroundAreaX1 && mouseX <= backgroundAreaX2) {
            if (button == 0) {
                for (int i = backgroundBlocks.size() - 1; i >= 0; i--) {
                    CustomBlock block = backgroundBlocks.get(i);
                    if (!block.isTrigger) continue;
                    int screenX = (int) ((block.x + offsetX) * scale + width / 2);
                    int screenY = (int) ((block.y + offsetY) * scale + height / 2);
                    int blockScreenWidth = (int) (block.width * scale);
                    int blockScreenHeight = (int) (block.height * scale);
                    int tfScreenHeight = (int) (14 * scale);
                    int tfScreenY = screenY + blockScreenHeight - tfScreenHeight - 4;
                    if (mouseX >= screenX + 4 && mouseX <= screenX + blockScreenWidth - 4 &&
                            mouseY >= tfScreenY && mouseY <= tfScreenY + tfScreenHeight) {
                        long currentTime = System.currentTimeMillis();
                        if (lastClickedTriggerBlock == block && (currentTime - lastInputClickTime) < 300) {
                            inputPanelActive = true;
                            selectedTriggerBlock = block;
                            selectedTriggerIndex = -1;
                            inputListScrollOffset = 0;
                            inputListSmoothScrollOffset = 0;
                            lastClickedTriggerBlock = null;
                            return true;
                        } else {
                            lastClickedTriggerBlock = block;
                            lastInputClickTime = currentTime;
                            return true;
                        }
                    }
                }
            }
            if (button == 1) {
                if (Screen.hasShiftDown()) {
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
                    double worldX = (backgroundContextMenuX - width / 2d) / scale - offsetX;
                    double worldY = (backgroundContextMenuY - height / 2d) / scale - offsetY;
                    CustomBlock newBlock = new CustomBlock((int) worldX, (int) worldY);
                    backgroundBlocks.add(newBlock);
                    backgroundContextMenuActive = false;
                    contextBlock = null;
                    return true;
                }
                if (mouseX >= menuX && mouseX <= menuX + CONTEXT_MENU_WIDTH &&
                        mouseY >= menuY + CONTEXT_OPTION_HEIGHT && mouseY < menuY + CONTEXT_OPTION_HEIGHT * 2) {
                    double worldX = (backgroundContextMenuX - width / 2d) / scale - offsetX;
                    double worldY = (backgroundContextMenuY - height / 2d) / scale - offsetY;
                    CustomBlock newTrigger = new CustomBlock((int) worldX, (int) worldY, true);
                    backgroundBlocks.add(newTrigger);
                    backgroundContextMenuActive = false;
                    contextBlock = null;
                    return true;
                }
                if (contextBlock != null &&
                        mouseX >= menuX && mouseX <= menuX + CONTEXT_MENU_WIDTH &&
                        mouseY >= menuY + CONTEXT_OPTION_HEIGHT * 2 && mouseY < menuY + CONTEXT_OPTION_HEIGHT * 3) {
                    backgroundBlocks.remove(contextBlock);
                    connections.removeIf(conn -> conn.start == contextBlock || conn.end == contextBlock);
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                                Text.of("Блок удалён, связи удалены"), false);
                    }
                    backgroundContextMenuActive = false;
                    contextBlock = null;
                    return true;
                }
                backgroundContextMenuActive = false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && !detailActive && !configOverlayActive && !inputPanelActive) {
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
        if (inputPanelActive) {
            int panelWidth = 200;
            int panelHeight = 150;
            int panelX = (width - panelWidth) / 2;
            int panelY = (height - panelHeight) / 2;
            int listAreaX = panelX + 10;
            int listAreaY = panelY + 30;
            int listAreaWidth = panelWidth - 20;
            int listAreaHeight = panelHeight - 30 - 40;
            if (mouseX >= listAreaX && mouseX <= listAreaX + listAreaWidth &&
                    mouseY >= listAreaY && mouseY <= listAreaY + listAreaHeight) {
                inputListScrollOffset = Math.max(0, Math.min(inputListScrollOffset - (int) (amount * 20), maxInputListScrollOffset));
                return true;
            }
        }
        if (configOverlayActive) {
            int overlayX = (width - configOverlayWidth) / 2;
            int listAreaY = (height - configOverlayHeight) / 2 + 40;
            int listAreaHeight = configOverlayHeight - 40 - 34;
            if (mouseX >= overlayX && mouseX <= overlayX + configOverlayWidth &&
                    mouseY >= listAreaY && mouseY <= listAreaY + listAreaHeight) {
                configListScrollOffset = Math.max(0, Math.min(configListScrollOffset - (int) (amount * 20), maxConfigListScrollOffset));
                return true;
            }
        }
        int rightPanelX = width - RIGHT_PANEL_WIDTH;
        if (!detailActive && mouseX >= rightPanelX && mouseX <= rightPanelX + RIGHT_PANEL_WIDTH) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int) (amount * SCROLL_SPEED), maxScrollOffset));
        } else {
            double oldTargetScale = targetScale;
            targetScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, targetScale + amount * 0.2));
            focusX = (mouseX - width / 2) / scale + offsetX;
            focusY = (mouseY - height / 2) / scale + offsetY;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     * Отрисовка модального окна (оверлея) для выбора конфигов с улучшенным дизайном.
     * Список файлов заполняется полностью тёмно-серым цветом, а кнопки "Открыть" и "Удалить"
     * перенесены в верхнюю панель.
     */
    private void renderConfigOverlay(DrawContext context, int mouseX, int mouseY) {
        int overlayX = (width - configOverlayWidth) / 2;
        int overlayY = (height - configOverlayHeight) / 2;
        // Фон оверлея
        context.fill(overlayX, overlayY, overlayX + configOverlayWidth, overlayY + configOverlayHeight, 0xAA000000);
        // Заголовок (верхняя панель)
        context.fill(overlayX, overlayY, overlayX + configOverlayWidth, overlayY + 30, 0xFF444444);
        context.drawTextWithShadow(textRenderer, Text.of("Путь: " + CONFIG_FOLDER), overlayX + 5, overlayY + 8, 0xFFFFFF);
        // Кнопка закрытия
        int closeButtonSize = 20;
        int closeX = overlayX + configOverlayWidth - closeButtonSize - 5;
        int closeY = overlayY + 5;
        context.fill(closeX, closeY, closeX + closeButtonSize, closeY + closeButtonSize, 0xFFFF0000);
        context.drawTextWithShadow(textRenderer, Text.of("X"), closeX + 6, closeY + 4, 0xFFFFFF);
        // Кнопки "Открыть" и "Удалить" в заголовке
        int buttonSize = 24;
        int openX = closeX - buttonSize - 5;
        int openY = overlayY + (30 - buttonSize) / 2;
        int deleteX = openX - buttonSize - 5;
        int deleteY = openY;
        context.drawTexture(OPEN_TEXTURE, openX, openY, 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);
        context.drawTexture(DELETE_TEXTURE, deleteX, deleteY, 0, 0, buttonSize, buttonSize, buttonSize, buttonSize);

        // Область списка файлов
        int listAreaY = overlayY + 40;
        int listAreaHeight = configOverlayHeight - 40 - 10;
        int lineHeight = 20;
        maxConfigListScrollOffset = Math.max(0, configFiles.size() * lineHeight - listAreaHeight);
        configListSmoothScrollOffset += (configListScrollOffset - configListSmoothScrollOffset) * 0.2;

        enableScissor(overlayX, listAreaY, configOverlayWidth, listAreaHeight);
        context.fill(overlayX, listAreaY, overlayX + configOverlayWidth, listAreaY + listAreaHeight, 0xFF111111);
        for (int i = 0; i < configFiles.size(); i++) {
            int fileY = listAreaY + i * lineHeight - (int) configListSmoothScrollOffset;
            if (fileY + lineHeight < listAreaY || fileY > listAreaY + listAreaHeight) continue;
            if (i == selectedConfigIndex) {
                context.fill(overlayX, fileY, overlayX + configOverlayWidth, fileY + lineHeight, 0xFF666666);
            } else if (mouseX >= overlayX && mouseX <= overlayX + configOverlayWidth &&
                    mouseY >= fileY && mouseY <= fileY + lineHeight) {
                context.fill(overlayX, fileY, overlayX + configOverlayWidth, fileY + lineHeight, 0xFF555555);
            }
            context.drawTextWithShadow(textRenderer, Text.of(configFiles.get(i).getName()), overlayX + 5, fileY + 4, 0xFFFFFF);
        }
        disableScissor();

        // Ползунок для списка
        if (maxConfigListScrollOffset > 0) {
            int scrollbarWidth = 6;
            int trackHeight = listAreaHeight;
            int scrollbarHeight = Math.max(20, (int) ((float) trackHeight / (configFiles.size() * lineHeight) * trackHeight));
            double scrollPercent = (double) configListSmoothScrollOffset / maxConfigListScrollOffset;
            int scrollbarY = listAreaY + (int) ((trackHeight - scrollbarHeight) * scrollPercent);
            int scrollbarX = overlayX + configOverlayWidth - scrollbarWidth;
            context.fill(scrollbarX, listAreaY, scrollbarX + scrollbarWidth, listAreaY + listAreaHeight, 0xFF222222);
            context.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0xFFAAAAAA);
        }
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
     * Загружает блоки (и связи) из указанного JSON-файла.
     */
    private void loadBlocks() {
        File configFile = new File(CONFIG_FOLDER, "blocks.json");
        if (configFile.exists()) {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(configFile)) {
                SavedData data = gson.fromJson(reader, SavedData.class);
                if (data != null) {
                    backgroundBlocks.clear();
                    backgroundBlocks.addAll(data.blocks);
                    connections.clear();
                    if (data.connections != null) {
                        for (ConnectionData cd : data.connections) {
                            if (cd.startIndex >= 0 && cd.startIndex < backgroundBlocks.size() &&
                                    cd.endIndex >= 0 && cd.endIndex < backgroundBlocks.size()) {
                                Connection conn = new Connection(backgroundBlocks.get(cd.startIndex), backgroundBlocks.get(cd.endIndex));
                                conn.particleProgress = cd.particleProgress;
                                conn.speed = cd.speed;
                                connections.add(conn);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Сохраняет блоки (и связи) в новый JSON-файл.
     */
    private void saveBlocks() {
        SavedData data = new SavedData();
        data.blocks = backgroundBlocks;
        data.connections = new ArrayList<>();
        for (Connection conn : connections) {
            ConnectionData cd = new ConnectionData();
            cd.startIndex = backgroundBlocks.indexOf(conn.start);
            cd.endIndex = backgroundBlocks.indexOf(conn.end);
            cd.particleProgress = conn.particleProgress;
            cd.speed = conn.speed;
            data.connections.add(cd);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);
        long time = System.currentTimeMillis();
        String filename = "blocks_" + time + ".json";
        File configFile = new File(CONFIG_FOLDER, filename);
        configFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
    }

    /**
     * Класс для кастомного блока (плашки) на фоне.
     * Поле isTrigger определяет, является ли элемент триггером.
     */
    private static class CustomBlock {
        int x, y, width, height;
        String blockText;
        String inputText;
        boolean isTrigger;

        CustomBlock(int x, int y) {
            this(x, y, false);
        }

        CustomBlock(int x, int y, boolean isTrigger) {
            this.x = x;
            this.y = y;
            this.width = 50;
            this.height = 50;
            this.isTrigger = isTrigger;
            this.blockText = isTrigger ? "Trigger" : "Block";
            this.inputText = "";
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

        Connection(CustomBlock start, CustomBlock end) {
            this.start = start;
            this.end = end;
            this.particleProgress = 0f;
            this.speed = 0.1f;
        }
    }

    /**
     * Класс для сохранения данных – блоков и связей.
     */
    private static class SavedData {
        List<CustomBlock> blocks;
        List<ConnectionData> connections;
    }

    /**
     * Класс для сохранения информации о соединении.
     * Хранит индексы блока-источника и блока-приёмника, а также параметры анимации.
     */
    private static class ConnectionData {
        int startIndex;
        int endIndex;
        float particleProgress;
        float speed;
    }

    /**
     * Рендерит зомби-entity в левом нижнем углу экрана.
     * Рендер статичен и не зависит от позиции мыши.
     */
    private void renderZombieEntity(DrawContext context) {
        // Позиция зомби: 20 пикселей от левого края, 80 пикселей от нижнего края
        int posX = 20;
        int posY = height - 20;
        // Фактор масштабирования (настройте по вкусу)
        float scaleFactor = 30.0F;

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        // Перемещаемся в нужную позицию с небольшим Z-сдвигом для корректного рендера
        matrixStack.translate(posX, posY, 105.0);
        // Инвертируем ось X и масштабируем
        matrixStack.scale(-scaleFactor, -scaleFactor, scaleFactor);
        // Поворачиваем по Y, чтобы зомби был лицом к игроку
        matrixStack.multiply(new Quaternionf().rotateY((float) Math.toRadians(360))); // 180 градусов поворота


        if (zombieEntity == null && MinecraftClient.getInstance().world != null) {
            zombieEntity = new ZombieEntity(EntityType.ZOMBIE, MinecraftClient.getInstance().world);
            zombieEntity.refreshPositionAndAngles(0, 0, 0, 180, 0);
        }

        MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderShadows(false);
        MinecraftClient.getInstance().getEntityRenderDispatcher().render(
                zombieEntity,
                0.0, 0.0, 0.0,
                0.0F, 1.0F,
                matrixStack,
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
                15728880);
        MinecraftClient.getInstance().getEntityRenderDispatcher().setRenderShadows(true);
        matrixStack.pop();
    }
}
