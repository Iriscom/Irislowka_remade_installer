package net.iristeam.storycore.client.gui.Iris;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Экран с Telegram‑подобным интерфейсом.
 * Список контактов скроллится плавно с инерцией и поддержкой перетаскивания мышью.
 */
public class iris_silitorScreen extends Screen {

    // Параметры современного дизайна для панели контактов
    private static final int CONTACTS_PANEL_WIDTH = 200;
    private static final int HEADER_HEIGHT = 40;
    // Оригинальное значение для высоты поля ввода
    private static final int INPUT_FIELD_HEIGHT = 30;
    private static final int PADDING = 15;
    private static final int CONTACT_HEIGHT = 30;

    // Цветовая палитра для панели контактов
    private static final int CONTACTS_PANEL_BG = 0xFF1E1E1E;
    private static final int CONTACT_ITEM_NORMAL = 0xFF2C2C2C;
    private static final int CONTACT_ITEM_HOVER = 0xFF3A3A3A;
    private static final int CONTACT_ITEM_SELECTED = 0xFF4A4A4A;
    private static final int CONTACT_TEXT_COLOR = 0xFFCCCCCC;

    // Константы для сообщений
    private static final int TEXT_MESSAGE_HEIGHT = 20;
    private static final int IMAGE_MESSAGE_MAX_HEIGHT = 100;
    private static final int IMAGE_MESSAGE_MAX_WIDTH = 100;
    private static final int MESSAGE_PADDING = 4;
    private static final float CHAT_SCROLL_SPEED = 0.3f; // для чата

    // Параметры скролла списка контактов
    private static final float CONTACTS_FRICTION = 0.9f;

    // Константы для увеличенного изображения
    private static final int ENLARGED_IMAGE_MAX_WIDTH = 300;
    private static final int ENLARGED_IMAGE_MAX_HEIGHT = 300;

    private Map<String, List<String>> chats = new HashMap<>();
    private final List<String> contacts = new ArrayList<>();
    private String currentContact = "";

    // ===============================
    // Виджеты экрана
    // ===============================
    private CustomTextFieldWidget messageField;
    private ButtonWidget sendButton;
    private ButtonWidget attachButton;

    private float targetChatScrollOffset = 0.0f;
    private float smoothChatScrollOffset = 0.0f;
    private float chatPanelAlpha = 1.0f;
    private float chatPanelTargetAlpha = 1.0f;

    private float contactsScrollOffset = 0.0f;
    private float contactsScrollVelocity = 0.0f;

    // Состояния анимации ховера для контактов
    private final Map<String, Float> contactHoverStates = new HashMap<>();

    // ===============================
    // Плашка выбора изображения
    // ===============================
    private boolean imagePickerOpen = false;
    private final List<LoadedImage> loadedImages = new ArrayList<>();

    private LoadedImage enlargedImage = null;

    // Хитбоксы для картинок в чате (для определения клика)
    private final List<ChatImageHitbox> chatImageHitboxes = new ArrayList<>();

    // Для поддержки перетаскивания мышью в списке контактов
    private boolean draggingContacts = false;
    private double lastDragY = 0;

    /**
     * Класс для хранения загруженных изображений.
     */
    private static class LoadedImage {
        public final Identifier texture;
        public final int width;
        public final int height;
        public final String fileName;
        public LoadedImage(Identifier texture, int width, int height, String fileName) {
            this.texture = texture;
            this.width = width;
            this.height = height;
            this.fileName = fileName;
        }
    }

    /**
     * Хитбокс для определения клика по изображению в чате.
     */
    private class ChatImageHitbox {
        public final int x, y, width, height;
        public final LoadedImage image;
        public ChatImageHitbox(int x, int y, int width, int height, LoadedImage image) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.image = image;
        }
        public boolean contains(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }
    }

    /**
     * Путь к файлу чатов (JSON).
     */
    private final Path chatsFilePath;

    /**
     * Конструктор.
     */
    public iris_silitorScreen() {
        super(Text.literal("Telegram-like GUI"));
        chatsFilePath = FabricLoader.getInstance().getGameDir().resolve("config/chats.json");
        loadChats();

        if (contacts.isEmpty()) {
            contacts.add("Alice");
            contacts.add("Bob");
            contacts.add("Charlie");
            contacts.add("Diana");
            contacts.add("Eve");
        }
        for (String contact : contacts) {
            chats.putIfAbsent(contact, new ArrayList<>());
        }
        if (currentContact.isEmpty() && !contacts.isEmpty()) {
            currentContact = contacts.get(0);
        }
        loadImages();
    }

    @Override
    protected void init() {
        super.init();
        this.children().clear();

        int chatAreaX = CONTACTS_PANEL_WIDTH;
        int chatAreaWidth = this.width - CONTACTS_PANEL_WIDTH;

        // ВОССТАНАВЛИВАЕМ ОРИГИНАЛЬНЫЕ РАЗМЕРЫ ПОЛЯ ВВОДА И КНОПОК
        int attachButtonWidth = 20;
        int sendButtonWidth = 50;
        int spacingBetweenButtons = 5;
        int messageFieldWidth = chatAreaWidth - PADDING * 2 - (attachButtonWidth + sendButtonWidth + spacingBetweenButtons * 2);
        int messageFieldX = chatAreaX + PADDING;
        int messageFieldY = this.height - INPUT_FIELD_HEIGHT - PADDING;

        messageField = new CustomTextFieldWidget(this.textRenderer, messageFieldX, messageFieldY, messageFieldWidth, 20, Text.literal("Type message..."));
        messageField.setMaxLength(200);
        messageField.setText("");
        messageField.setFocused(true);
        this.addDrawableChild(messageField);

        // Кнопка "📎"
        int attachButtonX = messageFieldX + messageFieldWidth + spacingBetweenButtons;
        int attachButtonY = messageFieldY;
        attachButton = ButtonWidget.builder(Text.literal("📎"), button -> imagePickerOpen = !imagePickerOpen)
                .dimensions(attachButtonX, attachButtonY, attachButtonWidth, 20)
                .build();
        this.addDrawableChild(attachButton);

        // Кнопка "Send"
        int sendButtonX = attachButtonX + attachButtonWidth + spacingBetweenButtons;
        int sendButtonY = messageFieldY;
        sendButton = ButtonWidget.builder(Text.literal("Send"), button -> onSendButtonPressed())
                .dimensions(sendButtonX, sendButtonY, sendButtonWidth, 20)
                .build();
        this.addDrawableChild(sendButton);

        targetChatScrollOffset = 0.0f;
        smoothChatScrollOffset = 0.0f;
        chatPanelAlpha = 1.0f;
        chatPanelTargetAlpha = 1.0f;

        contactsScrollOffset = 0.0f;
        contactsScrollVelocity = 0.0f;
    }

    private void renderImagePickerPanel(DrawContext context, int mouseX, int mouseY) {
        int panelWidth = this.width - 200;
        int panelHeight = 200;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = HEADER_HEIGHT + PADDING;
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC000000);
        context.drawText(this.textRenderer, "Select Image", panelX + PADDING, panelY + PADDING, 0xFFFFFFFF, false);

        int thumbSize = 50;
        int spacing = 10;
        int cols = Math.max((panelWidth - 2 * PADDING) / (thumbSize + spacing), 1);
        int x = panelX + PADDING;
        int y = panelY + PADDING + 15;
        int index = 0;
        for (LoadedImage li : loadedImages) {
            context.fill(x, y, x + thumbSize, y + thumbSize, 0xFF3A3A3A);
            context.drawTexture(li.texture, x, y, 0, 0, thumbSize, thumbSize, thumbSize, thumbSize);
            if (mouseX >= x && mouseX <= x + thumbSize && mouseY >= y && mouseY <= y + thumbSize) {
                context.fill(x, y, x + thumbSize, y + thumbSize, 0x80FFFFFF);
            }
            index++;
            if (index % cols == 0) {
                x = panelX + PADDING;
                y += thumbSize + spacing;
            } else {
                x += thumbSize + spacing;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Отрисовка базового фона
        this.renderBackground(context);

        // Отрисовка панели контактов
        renderContactsPanel(context, mouseX, mouseY, delta);
        // Отрисовка панели чата
        renderChatPanel(context, mouseX, mouseY, delta);

        messageField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        if (imagePickerOpen) {
            renderImagePickerPanel(context, mouseX, mouseY);
        }
        if (enlargedImage != null) {
            renderEnlargedImage(context, mouseX, mouseY);
        }
    }

    /**
     * Отрисовка увеличенного изображения по центру экрана.
     */
    private void renderEnlargedImage(DrawContext context, int mouseX, int mouseY) {
        context.fill(0, 0, this.width, this.height, 0x88000000);
        float ratio = enlargedImage.width / (float) enlargedImage.height;
        int dispWidth, dispHeight;
        if (ratio > 1) {
            dispWidth = ENLARGED_IMAGE_MAX_WIDTH;
            dispHeight = (int) (ENLARGED_IMAGE_MAX_WIDTH / ratio);
        } else {
            dispHeight = ENLARGED_IMAGE_MAX_HEIGHT;
            dispWidth = (int) (ENLARGED_IMAGE_MAX_HEIGHT * ratio);
        }
        int x = (this.width - dispWidth) / 2;
        int y = (this.height - dispHeight) / 2;
        context.fill(x - 2, y - 2, x + dispWidth + 2, y, 0xFFFFFFFF);
        context.fill(x - 2, y + dispHeight, x + dispWidth + 2, y + dispHeight + 2, 0xFFFFFFFF);
        context.fill(x - 2, y - 2, x, y + dispHeight + 2, 0xFFFFFFFF);
        context.fill(x + dispWidth, y - 2, x + dispWidth + 2, y + dispHeight + 2, 0xFFFFFFFF);
        context.drawTexture(enlargedImage.texture, x, y, 0, 0, dispWidth, dispHeight, dispWidth, dispHeight);
    }

    /**
     * Отрисовка списка контактов в современном стиле с плавной анимацией ховера.
     * После отрисовки списка дополнительно перерисовывается область заголовка,
     * чтобы контакты не перекрывали текст "CONTACTS".
     */
    private void renderContactsPanel(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelX = 0;
        int panelY = 0;
        int panelWidth = CONTACTS_PANEL_WIDTH;
        int panelHeight = this.height;

        // Фон панели – современный тёмный фон
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, CONTACTS_PANEL_BG);

        // Определяем область для списка контактов (без заголовка)
        int headerAreaHeight = PADDING + 20;
        int listTop = headerAreaHeight;
        int listBottom = panelHeight - PADDING;

        // Отрисовка каждого контакта с анимацией ховера
        for (int i = 0; i < contacts.size(); i++) {
            String contact = contacts.get(i);
            float yPos = listTop + i * CONTACT_HEIGHT - contactsScrollOffset;

            // Пропускаем, если элемент полностью вне видимой области списка
            if (yPos + CONTACT_HEIGHT < listTop || yPos > listBottom) continue;

            int left = PADDING;
            int right = CONTACTS_PANEL_WIDTH - PADDING;
            boolean isHovered = mouseX >= left && mouseX <= right && mouseY >= yPos && mouseY <= yPos + CONTACT_HEIGHT;

            // Плавное изменение состояния ховера
            float currentHover = contactHoverStates.getOrDefault(contact, 0f);
            float targetHover = isHovered ? 1f : 0f;
            float newHover = currentHover + (targetHover - currentHover) * 0.2f;
            contactHoverStates.put(contact, newHover);

            // Вычисляем цвет фона контакта с плавной анимацией
            int baseColor = interpolateColor(CONTACT_ITEM_NORMAL, CONTACT_ITEM_HOVER, newHover);
            if (contact.equals(currentContact)) {
                baseColor = CONTACT_ITEM_SELECTED;
            }

            // Рисуем скруглённый прямоугольник для фона контакта
            drawRoundedRect(context, left, (int) yPos, right - left, CONTACT_HEIGHT - 4, 8, baseColor);

            // Отрисовываем текст контакта
            context.drawText(this.textRenderer, contact, left + 15, (int) yPos + 8, CONTACT_TEXT_COLOR, false);
        }

        // Перерисовываем заголовок "CONTACTS" поверх списка, чтобы он не был перекрыт
        context.fill(panelX, panelY, panelX + panelWidth, headerAreaHeight, CONTACTS_PANEL_BG);
        context.drawText(this.textRenderer, "CONTACTS", panelX + PADDING, PADDING, 0xFFFFFFFF, false);
    }

    /**
     * Вспомогательный метод для линейной интерполяции двух ARGB-цветов.
     */
    private int interpolateColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Вспомогательный метод для отрисовки скруглённого прямоугольника.
     */
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        // Центральная часть
        context.fill(x + radius, y, x + width - radius, y + height, color);
        // Левые и правые боковые части
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
        // Верхняя и нижняя части
        context.fill(x + radius, y, x + width - radius, y + radius, color);
        context.fill(x + radius, y + height - radius, x + width - radius, y + height, color);
        // Углы
        context.fill(x, y, x + radius, y + radius, color);
        context.fill(x + width - radius, y, x + width, y + radius, color);
        context.fill(x, y + height - radius, x + radius, y + height, color);
        context.fill(x + width - radius, y + height - radius, x + width, y + height, color);
    }

    /**
     * Отрисовка панели чата с плавной прокруткой.
     * Здесь используется scissor (clipping) для ограничения области отрисовки сообщений,
     * чтобы они не перекрывали заголовок "Chat with ..." и "Custom Status".
     */
    private void renderChatPanel(DrawContext context, int mouseX, int mouseY, float delta) {
        int chatAreaX = CONTACTS_PANEL_WIDTH;
        context.fill(chatAreaX, 0, this.width, this.height, 0xFF1A1A1A);
        context.fill(chatAreaX, 0, this.width, HEADER_HEIGHT, 0xFF202020);
        context.drawText(this.textRenderer, "Chat with " + currentContact, chatAreaX + PADDING, 12, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, "Custom Status: Online", chatAreaX + PADDING, HEADER_HEIGHT + 5, 0xFF00FF00, false);

        int chatMessagesAreaY = HEADER_HEIGHT + PADDING + 20;
        int chatMessagesAreaHeight = this.height - HEADER_HEIGHT - INPUT_FIELD_HEIGHT - PADDING * 2 - 20;
        int alphaInt = ((int) (chatPanelAlpha * 255)) << 24;

        // Вычисляем область, в которой будут отрисовываться сообщения
        int scissorX = chatAreaX + PADDING;
        int scissorY = chatMessagesAreaY;
        int scissorWidth = this.width - chatAreaX - 2 * PADDING;
        int scissorHeight = chatMessagesAreaHeight;
        // Учёт масштабирования окна (scaleFactor)
        int scale = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
        RenderSystem.enableScissor(scissorX * scale, (this.height - (scissorY + scissorHeight)) * scale, scissorWidth * scale, scissorHeight * scale);

        List<String> messages = chats.getOrDefault(currentContact, new ArrayList<>());
        smoothChatScrollOffset += (targetChatScrollOffset - smoothChatScrollOffset) * CHAT_SCROLL_SPEED;
        float totalChatHeight = computeTotalChatHeight(messages);
        if (totalChatHeight <= chatMessagesAreaHeight) {
            targetChatScrollOffset = 0;
        } else if (targetChatScrollOffset > totalChatHeight - chatMessagesAreaHeight) {
            targetChatScrollOffset = totalChatHeight - chatMessagesAreaHeight;
        }
        chatImageHitboxes.clear();
        float y = chatMessagesAreaY - smoothChatScrollOffset;
        for (String msg : messages) {
            int msgHeight = TEXT_MESSAGE_HEIGHT;
            if (msg.startsWith("[IMG]")) {
                String fileName = msg.substring(5).trim();
                LoadedImage li = getLoadedImageByFileName(fileName);
                if (li != null) {
                    float ratio = li.width / (float) li.height;
                    int thumbWidth, thumbHeight;
                    if (ratio > 1) {
                        thumbWidth = IMAGE_MESSAGE_MAX_WIDTH;
                        thumbHeight = (int) (IMAGE_MESSAGE_MAX_WIDTH / ratio);
                    } else {
                        thumbHeight = IMAGE_MESSAGE_MAX_HEIGHT;
                        thumbWidth = (int) (IMAGE_MESSAGE_MAX_HEIGHT * ratio);
                    }
                    msgHeight = thumbHeight;
                    if (y + msgHeight >= chatMessagesAreaY && y <= chatMessagesAreaY + chatMessagesAreaHeight) {
                        context.fill(chatAreaX + PADDING, (int) y, chatAreaX + PADDING + thumbWidth, (int) (y + thumbHeight), 0xFF3A3A3A);
                        context.drawTexture(li.texture, chatAreaX + PADDING, (int) y, 0, 0, thumbWidth, thumbHeight, thumbWidth, thumbHeight);
                        chatImageHitboxes.add(new ChatImageHitbox(chatAreaX + PADDING, (int) y, thumbWidth, thumbHeight, li));
                    }
                } else {
                    if (y + TEXT_MESSAGE_HEIGHT >= chatMessagesAreaY && y <= chatMessagesAreaY + chatMessagesAreaHeight) {
                        context.drawText(this.textRenderer, "[IMG] " + fileName, chatAreaX + PADDING + 5, (int) y + 3, 0xFFFFFFFF, false);
                    }
                }
            } else {
                if (y + TEXT_MESSAGE_HEIGHT >= chatMessagesAreaY && y <= chatMessagesAreaY + chatMessagesAreaHeight) {
                    context.fill(chatAreaX + PADDING, (int) y, this.width - PADDING, (int) (y + TEXT_MESSAGE_HEIGHT - 2), (alphaInt | 0xFF3A3A3A));
                    context.drawText(this.textRenderer, msg, chatAreaX + PADDING + 5, (int) y + 3, 0xFFFFFFFF, false);
                }
            }
            y += msgHeight + MESSAGE_PADDING;
        }
        RenderSystem.disableScissor();
    }

    /**
     * Вычисляет общую высоту сообщений в чате.
     */
    private float computeTotalChatHeight(List<String> messages) {
        float total = 0;
        for (String msg : messages) {
            if (msg.startsWith("[IMG]")) {
                String fileName = msg.substring(5).trim();
                LoadedImage li = getLoadedImageByFileName(fileName);
                if (li != null) {
                    float ratio = li.width / (float) li.height;
                    int thumbHeight = (ratio > 1) ? (int) (IMAGE_MESSAGE_MAX_WIDTH / ratio) : IMAGE_MESSAGE_MAX_HEIGHT;
                    total += thumbHeight;
                } else {
                    total += TEXT_MESSAGE_HEIGHT;
                }
            } else {
                total += TEXT_MESSAGE_HEIGHT;
            }
            total += MESSAGE_PADDING;
        }
        return total;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // Если мышь над панелью контактов – обновляем скорость инерционного скролла (еще медленнее)
        if (mouseX < CONTACTS_PANEL_WIDTH) {
            contactsScrollVelocity -= amount * 5;
            return true;
        }
        // Скролл чата (и картинок в нем)
        int chatAreaX = CONTACTS_PANEL_WIDTH;
        int chatMessagesAreaY = HEADER_HEIGHT + PADDING + 20;
        int chatMessagesAreaHeight = this.height - HEADER_HEIGHT - INPUT_FIELD_HEIGHT - PADDING * 2 - 20;
        if (mouseX >= chatAreaX && mouseX <= this.width &&
                mouseY >= chatMessagesAreaY && mouseY <= chatMessagesAreaY + chatMessagesAreaHeight) {
            float totalHeight = computeTotalChatHeight(chats.getOrDefault(currentContact, new ArrayList<>()));
            targetChatScrollOffset -= amount * 10;
            if (targetChatScrollOffset < 0) targetChatScrollOffset = 0;
            float maxScroll = Math.max(totalHeight - chatMessagesAreaHeight, 0);
            if (targetChatScrollOffset > maxScroll) targetChatScrollOffset = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Если увеличенное изображение отображается – закрываем его при клике вне
        if (enlargedImage != null) {
            float ratio = enlargedImage.width / (float) enlargedImage.height;
            int dispWidth, dispHeight;
            if (ratio > 1) {
                dispWidth = ENLARGED_IMAGE_MAX_WIDTH;
                dispHeight = (int) (ENLARGED_IMAGE_MAX_WIDTH / ratio);
            } else {
                dispHeight = ENLARGED_IMAGE_MAX_HEIGHT;
                dispWidth = (int) (ENLARGED_IMAGE_MAX_HEIGHT * ratio);
            }
            int x = (this.width - dispWidth) / 2;
            int y = (this.height - dispHeight) / 2;
            if (!(mouseX >= x && mouseX <= x + dispWidth && mouseY >= y && mouseY <= y + dispHeight)) {
                enlargedImage = null;
                return true;
            }
            return true;
        }
        // Если открыта плашка выбора изображения – обрабатываем клики в ней
        if (imagePickerOpen) {
            int panelWidth = this.width - 200;
            int panelHeight = 200;
            int panelX = (this.width - panelWidth) / 2;
            int panelY = HEADER_HEIGHT + PADDING;
            if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
                    mouseY >= panelY && mouseY <= panelY + panelHeight) {
                int thumbSize = 50;
                int spacing = 10;
                int cols = Math.max((panelWidth - 2 * PADDING) / (thumbSize + spacing), 1);
                int x = panelX + PADDING;
                int y = panelY + PADDING + 15;
                int index = 0;
                for (LoadedImage li : loadedImages) {
                    if (mouseX >= x && mouseX <= x + thumbSize &&
                            mouseY >= y && mouseY <= y + thumbSize) {
                        List<String> msgs = chats.computeIfAbsent(currentContact, k -> new ArrayList<>());
                        msgs.add("[IMG] " + li.fileName);
                        saveChats();
                        imagePickerOpen = false;
                        chatPanelAlpha = 0.0f;
                        chatPanelTargetAlpha = 1.0f;
                        break;
                    }
                    index++;
                    if (index % cols == 0) {
                        x = panelX + PADDING;
                        y += thumbSize + spacing;
                    } else {
                        x += thumbSize + spacing;
                    }
                }
                return true;
            } else {
                imagePickerOpen = false;
                return true;
            }
        }
        // Обработка клика по контактам
        if (mouseX < CONTACTS_PANEL_WIDTH) {
            int listTop = PADDING + 20;
            int startY = listTop - (int) contactsScrollOffset;
            for (String contact : contacts) {
                int left = PADDING;
                int right = CONTACTS_PANEL_WIDTH - PADDING;
                if (mouseY >= startY && mouseY <= startY + CONTACT_HEIGHT &&
                        mouseX >= left && mouseX <= right) {
                    if (!contact.equals(currentContact)) {
                        currentContact = contact;
                        targetChatScrollOffset = 0;
                        chatPanelAlpha = 0.0f;
                        chatPanelTargetAlpha = 1.0f;
                    }
                    break;
                }
                startY += CONTACT_HEIGHT;
            }
            // Начинаем перетаскивание, если клик произошёл в области контактов
            draggingContacts = true;
            lastDragY = mouseY;
            return true;
        }
        // Клик по чату – проверяем попадание по изображению
        int chatAreaX = CONTACTS_PANEL_WIDTH;
        int chatMessagesAreaY = HEADER_HEIGHT + PADDING + 20;
        int chatMessagesAreaHeight = this.height - HEADER_HEIGHT - INPUT_FIELD_HEIGHT - PADDING * 2 - 20;
        if (mouseX >= chatAreaX && mouseX <= this.width &&
                mouseY >= chatMessagesAreaY && mouseY <= chatMessagesAreaY + chatMessagesAreaHeight) {
            for (ChatImageHitbox hitbox : chatImageHitboxes) {
                if (hitbox.contains((int) mouseX, (int) mouseY)) {
                    enlargedImage = hitbox.image;
                    return true;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
        messageField.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Завершаем перетаскивание списка контактов
        draggingContacts = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Если происходит перетаскивание в области контактов, обновляем смещение
        if (draggingContacts && mouseX < CONTACTS_PANEL_WIDTH) {
            double diff = mouseY - lastDragY;
            contactsScrollOffset -= diff;
            lastDragY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    /**
     * Обработка нажатия кнопки "Send" или клавиши ENTER.
     */
    private void onSendButtonPressed() {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            List<String> msgs = chats.computeIfAbsent(currentContact, k -> new ArrayList<>());
            msgs.add(text);
            messageField.setText("");
            saveChats();
            chatPanelAlpha = 0.0f;
            chatPanelTargetAlpha = 1.0f;
        }
    }

    /**
     * Сохраняет чаты в JSON-файл.
     */
    private void saveChats() {
        Gson gson = new Gson();
        String json = gson.toJson(chats);
        try {
            Files.createDirectories(chatsFilePath.getParent());
            Files.writeString(chatsFilePath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает чаты из JSON-файла.
     */
    private void loadChats() {
        if (Files.exists(chatsFilePath)) {
            try {
                String json = Files.readString(chatsFilePath, StandardCharsets.UTF_8);
                Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
                Map<String, List<String>> loadedChats = new Gson().fromJson(json, type);
                if (loadedChats != null) {
                    chats = loadedChats;
                    contacts.clear();
                    contacts.addAll(chats.keySet());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Загружает изображения из папки "screenshots" (PNG, JPG, JPEG).
     */
    private void loadImages() {
        Path screenshotsFolder = FabricLoader.getInstance().getGameDir().resolve("screenshots");
        if (!Files.exists(screenshotsFolder)) {
            System.out.println("Папка screenshots не найдена: " + screenshotsFolder);
            return;
        }
        try (var paths = Files.list(screenshotsFolder)) {
            paths.filter(path -> {
                String name = path.getFileName().toString().toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
            }).forEach(path -> {
                try {
                    NativeImage image = NativeImage.read(Files.newInputStream(path));
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                    Identifier textureId = MinecraftClient.getInstance().getTextureManager()
                            .registerDynamicTexture("screenshot_" + path.getFileName().toString(), texture);
                    String fileName = path.getFileName().toString();
                    LoadedImage li = new LoadedImage(textureId, image.getWidth(), image.getHeight(), fileName);
                    loadedImages.add(li);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ищет загруженное изображение по имени файла.
     */
    private LoadedImage getLoadedImageByFileName(String fileName) {
        for (LoadedImage li : loadedImages) {
            if (li.fileName.equals(fileName)) {
                return li;
            }
        }
        return null;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    // ===========================================================
    // Кастомное текстовое поле с анимацией фокуса
    // ===========================================================
    private class CustomTextFieldWidget extends TextFieldWidget {
        private float focusAnim = 0.0f;
        public CustomTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
            super(textRenderer, x, y, width, height, message);
        }
        @Override
        public void tick() {
            super.tick();
            float target = this.isFocused() ? 1.0f : 0.0f;
            focusAnim += (target - focusAnim) * 0.1f;
        }
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFFD3D3D3);
            if (focusAnim > 0.01f) {
                int alpha = (int)(focusAnim * 255);
                int borderColor = (alpha << 24) | 0x0000FF;
                int x = getX(), y = getY(), w = getWidth(), h = getHeight();
                context.fill(x, y, x + 2, y + h, borderColor);
                context.fill(x + w - 2, y, x + w, y + h, borderColor);
                context.fill(x, y, x + w, y + 2, borderColor);
                context.fill(x, y + h - 2, x + w, y + h, borderColor);
            }
            super.render(context, mouseX, mouseY, delta);
        }
    }

    /**
     * Метод tick() обновляет анимационные значения и инерционную прокрутку списка контактов.
     */
    @Override
    public void tick() {
        super.tick();

        // Обновление панели чата
        if (Math.abs(chatPanelAlpha - chatPanelTargetAlpha) > 0.01f) {
            chatPanelAlpha += (chatPanelTargetAlpha - chatPanelAlpha) * 0.05f;
        } else {
            chatPanelAlpha = chatPanelTargetAlpha;
        }
        messageField.tick();

        // Обновление инерционного скролла списка контактов
        contactsScrollOffset += contactsScrollVelocity;
        contactsScrollVelocity *= CONTACTS_FRICTION;

        // Ограничиваем скролл, чтобы не выйти за пределы списка
        int listTop = PADDING + 20;
        int listBottom = this.height - PADDING;
        int totalContactsHeight = contacts.size() * CONTACT_HEIGHT;
        float maxOffset = Math.max(totalContactsHeight - (listBottom - listTop), 0);
        if (contactsScrollOffset < 0) {
            contactsScrollOffset = 0;
            contactsScrollVelocity = 0;
        }
        if (contactsScrollOffset > maxOffset) {
            contactsScrollOffset = maxOffset;
            contactsScrollVelocity = 0;
        }
    }
}
