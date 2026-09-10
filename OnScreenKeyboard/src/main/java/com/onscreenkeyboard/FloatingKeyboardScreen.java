package com.onscreenkeyboard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Floating typing-only keyboard. It overlays the existing Minecraft screen,
 * so the keyboard is rendered after the underlying screen and stays on top.
 */
public final class FloatingKeyboardScreen extends Screen {
    private final @Nullable Screen parent;
    private @Nullable TextFieldWidget target;

    private int page = 1;
    private boolean capsLock = false;

    // Requested default size: 150 x 200.
    private double scale = 1.0;
    private int x = 12;
    private int y = 12;
    private boolean dragging = false;
    private double dragOffsetX;
    private double dragOffsetY;

    private static final int BASE_WIDTH = 150;
    private static final int BASE_HEIGHT = 200;
    private static final int BAR_HEIGHT = 20;
    private static final int GAP = 2;

    private static final String[] PAGE_1 = {
            "q w e r t y u i o p",
            "a s d f g h j k l",
            "⬆ z x c v b n m ⌫",
            "123 SPACE . ↵"
    };

    private static final String[] PAGE_2 = {
            "1 2 3 4 5 6 7 8 9 0",
            "@ # £ _ & - + ( ) /",
            "🔱 * \" ' : ; ! ? ⌫",
            "ABC , SPACE . ↵"
    };

    private static final String[] PAGE_3 = {
            "~ ` | • √ π ÷ × § ∆",
            "€ ¥ $ ¢ ^ ° = { } \\",
            "123 % © ® ™ ✓ [ ] ⌫",
            "ABC < SPACE > ↵"
    };

    private FloatingKeyboardScreen(MinecraftClient client, @Nullable Screen parent, @Nullable TextFieldWidget target) {
        super(Text.literal("On-Screen Keyboard"));
        this.parent = parent;
        this.target = target;
    }

    public static void open(MinecraftClient client) {
        Screen parent = client.currentScreen;
        TextFieldWidget target = findFocusedTextField(parent);
        client.setScreen(new FloatingKeyboardScreen(client, parent, target));
    }

    private static @Nullable TextFieldWidget findFocusedTextField(@Nullable Screen screen) {
        if (screen == null) return null;
        Element focused = screen.getFocused();
        TextFieldWidget direct = findTextField(focused);
        if (direct != null) return direct;
        return findFocusedInElements(screen.children());
    }

    private static @Nullable TextFieldWidget findFocusedInElements(List<? extends Element> elements) {
        for (Element element : elements) {
            TextFieldWidget field = findTextField(element);
            if (field != null) return field;
            if (element instanceof ParentElement parent) {
                TextFieldWidget nested = findFocusedInElements(parent.children());
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private static @Nullable TextFieldWidget findTextField(@Nullable Element element) {
        if (element instanceof TextFieldWidget field && field.isFocused() && field.isActive()) {
            return field;
        }
        return null;
    }

    @Override
    protected void init() {
        // No widgets are added. Keeping the keyboard as a custom overlay means
        // it cannot accidentally get buried under other Minecraft widgets.
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void closeKeyboard() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void tick() {
        if (parent != null) parent.tick();
        if (target == null || !target.isActive()) {
            target = findFocusedTextField(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // Render the original screen first, then the keyboard. This guarantees
        // the keyboard is visually above buttons, blocks, menus and overlays.
        if (parent != null) {
            parent.render(context, mouseX, mouseY, deltaTicks);
        }

        int w = width();
        int h = height();
        int left = x;
        int top = y;

        // Shadow + opaque-ish panel.
        context.fill(left + 3, top + 3, left + w + 3, top + h + 3, 0x90000000);
        context.fill(left, top, left + w, top + h, 0xE8171717);
        context.fill(left, top, left + w, top + BAR_HEIGHT, 0xFF252525);

        context.drawTextWithShadow(client.textRenderer, "Keyboard", left + 6, top + 6, 0xFFFFFFFF);
        drawButton(context, left + w - 40, top + 3, 18, 14, "±", mouseX, mouseY);
        drawButton(context, left + w - 20, top + 3, 18, 14, "×", mouseX, mouseY);

        int bodyTop = top + BAR_HEIGHT + 3;
        String[][] rows = rowsForPage();
        int rowCount = 4;
        int rowHeight = Math.max(1, (h - BAR_HEIGHT - 5 - GAP * (rowCount - 1)) / rowCount);

        for (int row = 0; row < rowCount; row++) {
            String[] keys = splitRow(rows[row]);
            drawRow(context, keys, left + 3, bodyTop + row * (rowHeight + GAP), w - 6, rowHeight, mouseX, mouseY);
        }

        if (target == null) {
            context.drawTextWithShadow(client.textRenderer, "Select a text box", left + 5, top + h - 7, 0xFFFFCC66);
        }
    }

    private String[][] rowsForPage() {
        if (page == 1) return new String[][]{
                {"q","w","e","r","t","y","u","i","o","p"},
                {"a","s","d","f","g","h","j","k","l"},
                {"⬆","z","x","c","v","b","n","m","⌫"},
                {"123","SPACE",".","↵"}
        };
        if (page == 2) return new String[][]{
                {"1","2","3","4","5","6","7","8","9","0"},
                {"@","#","£","_","&","-","+","(",")","/"},
                {"🔱","*","\"","'",":",";","!","?","⌫"},
                {"ABC",",","SPACE",".","↵"}
        };
        return new String[][]{
                {"~","`","|","•","√","π","÷","×","§","∆"},
                {"€","¥","$","¢","^","°","=","{","}","\\"},
                {"123","%","©","®","™","✓","[","]","⌫"},
                {"ABC","<","SPACE",">","↵"}
        };
    }

    private String[] splitRow(String row) {
        return row.split(" ");
    }

    private void drawRow(DrawContext context, String[] keys, int rowX, int rowY, int rowWidth, int rowHeight, int mouseX, int mouseY) {
        int n = keys.length;
        int gap = 1;
        int available = rowWidth - gap * (n - 1);
        int keyWidth = Math.max(1, available / n);
        int remainder = available - keyWidth * n;
        int currentX = rowX;

        for (int i = 0; i < n; i++) {
            int thisWidth = keyWidth + (i < remainder ? 1 : 0);
            String key = keys[i];
            if (capsLock && page == 1 && key.length() == 1 && Character.isLetter(key.charAt(0))) {
                key = key.toUpperCase();
            }
            drawButton(context, currentX, rowY, thisWidth, rowHeight, key, mouseX, mouseY);
            currentX += thisWidth + gap;
        }
    }

    private void drawButton(DrawContext context, int bx, int by, int bw, int bh, String label, int mouseX, int mouseY) {
        boolean hovered = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
        int bg = hovered ? 0xFF555555 : 0xFF303030;
        if (label.equals("×")) bg = hovered ? 0xFF9A3333 : 0xFF702222;
        if (label.equals("±")) bg = hovered ? 0xFF4E6A92 : 0xFF304563;
        context.fill(bx, by, bx + bw, by + bh, bg);
        context.fill(bx + 1, by + 1, bx + bw - 1, by + 2, 0xFF686868);

        int tw = client.textRenderer.getWidth(label);
        int tx = bx + Math.max(1, (bw - tw) / 2);
        int ty = by + Math.max(1, (bh - 9) / 2);
        context.drawTextWithShadow(client.textRenderer, label, tx, ty, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;

        double mx = click.x();
        double my = click.y();
        int left = x;
        int top = y;
        int w = width();
        int h = height();

        if (inside(mx, my, left + w - 20, top + 3, 18, 14)) {
            closeKeyboard();
            return true;
        }
        if (inside(mx, my, left + w - 40, top + 3, 18, 14)) {
            resizeKeyboard();
            return true;
        }
        if (inside(mx, my, left, top, w, BAR_HEIGHT)) {
            dragging = true;
            dragOffsetX = mx - x;
            dragOffsetY = my - y;
            return true;
        }

        int bodyTop = top + BAR_HEIGHT + 3;
        int rowHeight = Math.max(1, (h - BAR_HEIGHT - 5 - GAP * 3) / 4);
        if (my >= bodyTop && my < top + h) {
            int row = (int)((my - bodyTop) / (double)(rowHeight + GAP));
            if (row >= 0 && row < 4) {
                String[] keys = rowsForPage()[row];
                int gap = 1;
                int available = w - 6 - gap * (keys.length - 1);
                int keyWidth = Math.max(1, available / keys.length);
                int remainder = available - keyWidth * keys.length;
                int currentX = left + 3;
                for (int i = 0; i < keys.length; i++) {
                    int thisWidth = keyWidth + (i < remainder ? 1 : 0);
                    if (inside(mx, my, currentX, bodyTop + row * (rowHeight + GAP), thisWidth, rowHeight)) {
                        pressKey(keys[i]);
                        return true;
                    }
                    currentX += thisWidth + gap;
                }
            }
        }

        // Do not pass clicks through the keyboard: it is a dedicated typing control.
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            x = clamp((int)(click.x() - dragOffsetX), 0, Math.max(0, this.width - width()));
            y = clamp((int)(click.y() - dragOffsetY), 0, Math.max(0, this.height - height()));
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = false;
        return true;
    }

    private void pressKey(String key) {
        if (key.equals("⬆")) {
            capsLock = !capsLock;
            return;
        }
        if (key.equals("⌫")) {
            if (target != null) target.eraseCharacters(-1);
            return;
        }
        if (key.equals("123")) {
            page = 2;
            return;
        }
        if (key.equals("ABC")) {
            page = 1;
            return;
        }
        if (key.equals("🔱")) {
            page = page == 2 ? 3 : 2;
            return;
        }
        if (key.equals("SPACE")) {
            type(" ");
            return;
        }
        if (key.equals("↵")) {
            sendEnter();
            return;
        }

        if (page == 1 && key.length() == 1 && Character.isLetter(key.charAt(0))) {
            type(capsLock ? key.toUpperCase() : key.toLowerCase());
        } else {
            type(key);
        }
    }

    private void type(String text) {
        if (target == null || !target.isActive()) {
            target = findFocusedTextField(parent);
        }
        if (target != null && target.isActive()) {
            target.charTyped(new CharInput(text.codePointAt(0), 0));
        }
    }

    private void sendEnter() {
        if (parent != null) {
            parent.keyPressed(new KeyInput(GLFW.GLFW_KEY_ENTER, 0, 0));
        } else if (target != null) {
            target.keyPressed(new KeyInput(GLFW.GLFW_KEY_ENTER, 0, 0));
        }
    }

    private void resizeKeyboard() {
        scale += 0.2;
        if (scale > 1.8) scale = 0.6;
    }

    private int width() {
        return (int)Math.round(BASE_WIDTH * scale);
    }

    private int height() {
        return (int)Math.round(BASE_HEIGHT * scale);
    }

    private static boolean inside(double px, double py, int bx, int by, int bw, int bh) {
        return px >= bx && px < bx + bw && py >= by && py < by + bh;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.getKeycode() == GLFW.GLFW_KEY_ESCAPE) {
            closeKeyboard();
            return true;
        }
        // The physical K key is consumed by the mixin; this also supports K
        // from launchers that route custom controls through Minecraft's key input.
        return true;
    }
}
